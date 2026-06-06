# -*- coding: utf-8 -*-
"""
Insert dev-journey.md as a blog article into CyberBlog database via API.
"""
from __future__ import annotations
import json
import urllib.request
import urllib.error
import sys

BASE_URL = "http://localhost:8080"


def post_json(url: str, data: dict, token: str | None = None):
    body = json.dumps(data, ensure_ascii=False).encode("utf-8")
    headers = {"Content-Type": "application/json; charset=utf-8"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(url, data=body, headers=headers, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        print(f"  HTTP {e.code}: {e.read().decode('utf-8', errors='ignore')[:300]}")
        return None
    except Exception as exc:
        print(f"  Error: {exc}")
        return None


def main():
    # Read the markdown file
    with open("dev-journey.md", "r", encoding="utf-8") as f:
        content = f.read()

    print("=" * 55)
    print("  Inserting dev-journey.md into CyberBlog database")
    print("=" * 55)

    # 1. Authenticate
    print("\n[1/3] Authenticating...")
    login = post_json(f"{BASE_URL}/api/auth/login", {
        "username": "cyberadmin",
        "password": "Cyber@2077"
    })

    token = None
    if login and login.get("code") == 200:
        token = login["data"]["token"]
        print("  OK: logged in as cyberadmin")
    else:
        # Try register
        print("  Login failed, trying register...")
        reg = post_json(f"{BASE_URL}/api/auth/register", {
            "username": "cyberadmin",
            "email": "admin@cyberblog.io",
            "password": "Cyber@2077"
        })
        if reg and reg.get("code") == 200:
            token = reg["data"]["token"]
            print("  OK: registered and logged in")
        else:
            print("  FAIL: cannot authenticate. Is the backend running at http://localhost:8080?")
            sys.exit(1)

    # 2. Insert article
    print("\n[2/3] Inserting article...")
    resp = post_json(
        f"{BASE_URL}/api/articles",
        {
            "title": "赛博朋克博客开发全记录：一个人 + AI，从零到上线",
            "content": content,
            "contentType": "markdown",
            "tags": "开发日志,AI协作,经验分享,CyberBlog",
            "summary": "本文不是技术教程，而是一份人-AI 协作的实战日志。记录了从零开发赛博朋克博客的全过程：需求描述的艺术、点赞功能的两次翻车、容器化与一键部署、Windows 编码/TS/端口三连坑，以及总结出的六条人+AI协作铁律。",
            "status": "published",
        },
        token=token,
    )

    if resp and resp.get("code") == 200:
        article_id = resp["data"]["id"]
        print(f"  OK: article created (id={article_id})")
    else:
        print(f"  FAIL")
        sys.exit(1)

    # 3. Done
    print(f"\n[3/3] Done!")
    print(f"  Visit http://localhost to read the article")
    print("=" * 55)


if __name__ == "__main__":
    main()
