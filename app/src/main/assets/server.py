# ==============================================================================
#  [SERVER] 栞・既読位置サーバー同期スクリプト
#  Tech Stack : Python (FastAPI + SQLite)
# ==============================================================================

import base64
import os
import secrets
import sqlite3
import time
from typing import List, Optional, Union

from fastapi import FastAPI, HTTPException, Request, Response, status
from pydantic import BaseModel

# ------------------------------------------------------------------------------
#  [GUIDE] 起動手順
#  1. 初回時 (必須ライブラリインストール) : python -m pip install fastapi uvicorn
#  2. サーバー起動 : python server.py
# ------------------------------------------------------------------------------


# ==============================================================================
#  [CONFIG] 基本設定
# ==============================================================================

# [NOTE] サーバーの起動設定
# ※ ポート番号はComittoNxX側の設定と一致させてください。
SERVER_HOST = "0.0.0.0"
SERVER_PORT = 8081

# [NOTE] Basic認証を使用する場合に指定します
AUTH_USER: Optional[str] = None
AUTH_PASS: Optional[str] = None

# [NOTE] 書庫フォルダのルートパス (環境に合わせて書き換えてください)
# 例: "/mnt/storage/books" や "C:/Users/Username/Books" など
LIBRARY_DIR = "C:/Users/Username/Books"

# [NOTE] スキャン対象とする拡張子の一覧
TARGET_EXTENSIONS = ('.zip', '.cbz', '.rar', '.cbr', '.pdf', '.epub')

# [NOTE] ComittoNxXの「サーバーの選択」に登録しているSMBサーバーのホスト情報
SMB_HOST_NAME = "192.168.1.1"


# ==============================================================================
#  [DATABASE] SQLite 初期化・接続管理
# ==============================================================================

DB_PATH = "sync.db"


def init_sqlite_db():
    conn   = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # --------------------------------------------------------------------------
    #  1. 栞 (bookmarks) テーブル
    # --------------------------------------------------------------------------
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS bookmarks (
            host     TEXT    NOT NULL,
            path     TEXT    NOT NULL,
            file     TEXT    NOT NULL,
            page     INTEGER NOT NULL,
            image    TEXT,
            chapter  INTEGER,
            pagerate INTEGER,
            dispname TEXT,
            type     INTEGER,
            date     INTEGER,
            PRIMARY KEY (host, path, file, page)
        );
    """)

    # --------------------------------------------------------------------------
    #  2. 既読位置 (read_position) テーブル
    # --------------------------------------------------------------------------
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS read_position (
            host     TEXT    NOT NULL,
            path     TEXT    NOT NULL,
            file     TEXT    NOT NULL,
            page     INTEGER NOT NULL,
            maxpage  INTEGER,
            chapter  INTEGER,
            pagerate INTEGER,
            date     INTEGER,
            PRIMARY KEY (host, path, file)
        );
    """)

    # --------------------------------------------------------------------------
    #  3. 読書履歴 (history) テーブル
    # --------------------------------------------------------------------------
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS history (
            host     TEXT    NOT NULL,
            path     TEXT    NOT NULL,
            file     TEXT    NOT NULL,
            page     INTEGER,
            maxpage  INTEGER,
            image    TEXT,
            chapter  INTEGER,
            pagerate REAL,
            dispname TEXT,
            type     INTEGER,
            date     INTEGER,
            PRIMARY KEY (host, path, file)
        );
    """)

    # --------------------------------------------------------------------------
    #  4. 書庫カタログ (library) テーブル
    # --------------------------------------------------------------------------
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS library (
            path          TEXT NOT NULL,
            name          TEXT NOT NULL,
            size          INTEGER,
            date_modified INTEGER,
            work_title    TEXT,
            PRIMARY KEY (path, name)
        );
    """)

    conn.commit()
    conn.close()


# 起動時にデータベースを自動初期化
init_sqlite_db()


# ==============================================================================
#  [MODELS] データモデル定義 (Pydantic)
# ==============================================================================

class Bookmark(BaseModel):
    host:     str
    path:     str
    file:     str
    page:     int
    image:    Optional[str] = ""
    chapter:  Optional[int] = -1
    pagerate: Optional[int] = -1
    dispname: Optional[str] = ""
    type:     Optional[int] = 0
    date:     Optional[int] = 0


class BookmarkDeleteKey(BaseModel):
    host: str
    path: str
    file: str
    page: int


class ReadPosition(BaseModel):
    host:     str
    path:     str
    file:     str
    page:     int
    maxpage:  Optional[int] = 0
    chapter:  Optional[int] = -1
    pagerate: Optional[int] = -1
    date:     Optional[int] = 0


class HistoryItem(BaseModel):
    host:     str
    path:     str
    file:     str
    page:     Optional[int]   = 0
    maxpage:  Optional[int]   = 0
    image:    Optional[str]   = ""
    chapter:  Optional[int]   = -1
    pagerate: Optional[float] = -1.0  # Android側が double(pagerate) のため
    dispname: Optional[str]   = ""
    type:     Optional[int]   = 0
    date:     Optional[int]   = 0


class LibraryItem(BaseModel):
    path:          str           = ""
    name:          str           = ""
    size:          int           = 0
    date_modified: int           = 0
    work_title:    Optional[str] = ""


class LibraryPage(BaseModel):
    total:   int               = 0
    offset:  int               = 0
    limit:   int               = 500
    results: List[LibraryItem] = []


# ==============================================================================
#  [APP] FastAPI アプリケーション本体 & ミドルウェア
# ==============================================================================

app = FastAPI(title="ComittoNxX Bookmark & Sync Server")


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


# HTTP Basic 認証チェック処理
@app.middleware("http")
async def check_basic_auth(request: Request, call_next):
    if request.url.path == "/health":
        return await call_next(request)

    if AUTH_USER and AUTH_PASS:
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Basic "):
            return Response(
                status_code=status.HTTP_401_UNAUTHORIZED,
                headers={"WWW-Authenticate": "Basic"},
            )
        try:
            encoded_credentials = auth_header.split(" ")[1]
            decoded_credentials = base64.b64decode(encoded_credentials).decode("utf-8")
            username, password  = decoded_credentials.split(":", 1)

            is_user_correct = secrets.compare_digest(username, AUTH_USER)
            is_pass_correct = secrets.compare_digest(password, AUTH_PASS)

            if not (is_user_correct and is_pass_correct):
                return Response(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    headers={"WWW-Authenticate": "Basic"},
                )
        except Exception:
            return Response(
                status_code=status.HTTP_401_UNAUTHORIZED,
                headers={"WWW-Authenticate": "Basic"},
            )

    return await call_next(request)


# ==============================================================================
#  [HELPERS] スキャン処理
# ==============================================================================

def run_library_scan():
    if not os.path.exists(LIBRARY_DIR):
        print(f"[WARNING] LIBRARY_DIR '{LIBRARY_DIR}' does not exist.")
        return

    items = []
    for root, _, files in os.walk(LIBRARY_DIR):
        for file in files:
            if file.lower().endswith(TARGET_EXTENSIONS):
                full_path = os.path.join(root, file)
                try:
                    stat = os.stat(full_path)

                    # ディレクトリの相対パスを取得
                    rel_path = os.path.relpath(root, LIBRARY_DIR).replace("\\", "/")

                    # パスの先頭に SMB ホスト名を付加する
                    if rel_path == ".":
                        db_path = SMB_HOST_NAME
                    else:
                        db_path = f"{SMB_HOST_NAME}/{rel_path}"

                    items.append((
                        db_path,
                        file,
                        stat.st_size,
                        int(stat.st_mtime),
                        ""
                    ))
                except Exception as e:
                    print(f"[ERROR] Reading file {full_path}: {e}")

    # トランザクションでDBを丸ごと一括更新
    conn   = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("DELETE FROM library;")
    cursor.executemany("""
        INSERT INTO library (path, name, size, date_modified, work_title)
        VALUES (?, ?, ?, ?, ?)
    """, items)
    conn.commit()
    conn.close()
    print(f"[INFO] Scan completed. Total {len(items)} items indexed.")


# ==============================================================================
#  [ENDPOINTS] ルーター定義
# ==============================================================================

@app.get("/health")
def health_check():
    return {"status": "ok"}


# --- 栞 (bookmarks) API ---

@app.get("/bookmarks", response_model=List[Bookmark])
def get_bookmarks(host: Optional[str] = None):
    conn   = get_db()
    cursor = conn.cursor()
    if host:
        cursor.execute("SELECT * FROM bookmarks WHERE host = ?", (host,))
    else:
        cursor.execute("SELECT * FROM bookmarks")
    rows = cursor.fetchall()
    conn.close()
    return [dict(row) for row in rows]


@app.post("/bookmarks")
def upsert_bookmark(item: Bookmark):
    conn   = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO bookmarks (host, path, file, page, image, chapter, pagerate, dispname, type, date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(host, path, file, page) DO UPDATE SET
            image    = excluded.image,
            chapter  = excluded.chapter,
            pagerate = excluded.pagerate,
            dispname = excluded.dispname,
            type     = excluded.type,
            date     = excluded.date
    """, (
        item.host, item.path, item.file, item.page,
        item.image, item.chapter, item.pagerate, item.dispname, item.type, item.date
    ))
    conn.commit()
    conn.close()
    return {"status": "ok"}


@app.delete("/bookmarks")
def delete_bookmark(item: BookmarkDeleteKey):
    conn   = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        DELETE FROM bookmarks 
        WHERE host = ? AND path = ? AND file = ? AND page = ?
    """, (item.host, item.path, item.file, item.page))
    conn.commit()
    conn.close()
    return {"status": "ok"}


# --- 既読位置 (read_position) API ---

@app.get("/read_position", response_model=Union[ReadPosition, List[ReadPosition]])
def get_read_position(host: Optional[str] = None, file: Optional[str] = None, path: Optional[str] = ""):
    conn   = get_db()
    cursor = conn.cursor()

    if file:
        if path:
            cursor.execute("""
                SELECT * FROM read_position 
                WHERE host = ? AND path = ? AND file = ?
            """, (host, path, file))
        else:
            cursor.execute("""
                SELECT * FROM read_position 
                WHERE host = ? AND file = ?
            """, (host, file))
        row = cursor.fetchone()
        conn.close()

        if not row:
            raise HTTPException(status_code=404, detail="Read position not found")
        return dict(row)

    else:
        if host:
            cursor.execute("SELECT * FROM read_position WHERE host = ?", (host,))
        else:
            cursor.execute("SELECT * FROM read_position")
        rows = cursor.fetchall()
        conn.close()
        return [dict(row) for row in rows]


@app.post("/read_position")
def upsert_read_position(item: ReadPosition):
    conn   = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO read_position (host, path, file, page, maxpage, chapter, pagerate, date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(host, path, file) DO UPDATE SET
            page     = excluded.page,
            maxpage  = excluded.maxpage,
            chapter  = excluded.chapter,
            pagerate = excluded.pagerate,
            date     = excluded.date
    """, (
        item.host, item.path, item.file, item.page,
        item.maxpage, item.chapter, item.pagerate, item.date
    ))
    conn.commit()
    conn.close()
    return {"status": "ok"}


# --- 履歴 (history) API ---

@app.post("/history")
def upsert_history(item: HistoryItem):
    conn   = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO history (host, path, file, page, maxpage, image, chapter, pagerate, dispname, type, date)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT(host, path, file) DO UPDATE SET
            page     = excluded.page,
            maxpage  = excluded.maxpage,
            image    = excluded.image,
            chapter  = excluded.chapter,
            pagerate = excluded.pagerate,
            dispname = excluded.dispname,
            type     = excluded.type,
            date     = excluded.date
    """, (
        item.host, item.path, item.file, item.page, item.maxpage,
        item.image, item.chapter, item.pagerate, item.dispname, item.type, item.date
    ))
    conn.commit()
    conn.close()
    return {"status": "ok"}


# --- ライブラリ (library) API ---

@app.get("/library", response_model=LibraryPage)
def get_library(offset: int = 0, limit: int = 500):
    conn   = get_db()
    cursor = conn.cursor()

    # 総件数の取得
    cursor.execute("SELECT COUNT(*) FROM library")
    total = cursor.fetchone()[0]

    # ページネーション指定でデータ取得
    cursor.execute("SELECT path, name, size, date_modified, work_title FROM library LIMIT ? OFFSET ?", (limit, offset))
    rows = cursor.fetchall()
    conn.close()

    results = [LibraryItem(**dict(row)) for row in rows]

    return LibraryPage(
        total   = total,
        offset  = offset,
        limit   = limit,
        results = results
    )


@app.post("/library/rescan", status_code=status.HTTP_202_ACCEPTED)
def rescan_library():
    # バックグラウンド/同期処理でフォルダをスキャン
    run_library_scan()
    return {"status": "ok"}


# ==============================================================================
#  [MAIN] エントリーポイント
# ==============================================================================

if __name__ == "__main__":
    import uvicorn

    uvicorn.run("__main__:app", host=SERVER_HOST, port=SERVER_PORT)
