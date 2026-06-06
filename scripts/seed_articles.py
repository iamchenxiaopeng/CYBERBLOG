# -*- coding: utf-8 -*-
"""
CyberBlog seed script - insert AI articles via API
Usage: python seed_articles.py
"""
from __future__ import annotations
import json
import time
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080"

ARTICLES = [
    {
        "title": "大模型时代：当 GPT 开始思考，人类还剩什么？",
        "tags": "AI,大语言模型,哲学,未来",
        "summary": "从 Transformer 到 GPT-4o，大模型正在颠覆我们对智能的定义。本文深度探讨：当机器开始推理，人类的不可替代性在哪里？",
        "content": (
            "# 大模型时代：当 GPT 开始思考，人类还剩什么？\n\n"
            "> *工具从来不是威胁，威胁来自不懂工具的人。*\n\n"
            "## 一、从统计到推理：语言模型的范式跃迁\n\n"
            "2017 年，Google 发布 *Attention Is All You Need*，Transformer 架构横空出世。"
            "没有人预料到，短短 6 年后，这篇论文会催生出能写代码、做诗、解微积分的「怪物」。\n\n"
            "早期的语言模型（n-gram、LSTM）本质上是**统计机器**——它们学习的是「这个词之后最可能出现什么词」。"
            "但 GPT 系列从 GPT-3 开始展现出了完全不同的能力：**上下文学习（In-context Learning）**。\n\n"
            "```python\n"
            "# GPT-3 的涌现能力示例：zero-shot 推理\n"
            "prompt = \"\"\"\n"
            "将以下英文翻译为法文：\n"
            "sea otter => loutre de mer\n"
            "cheese => ?\n"
            "\"\"\"\n"
            "# 模型输出: fromage  ✓\n"
            "```\n\n"
            "没有任何明确的翻译训练，模型仅凭 prompt 中的「模式」就完成了推理。"
            "这在 2020 年几乎震惊了整个 NLP 社区。\n\n"
            "## 二、涌现能力：规模的魔法\n\n"
            "OpenAI 研究员在 2022 年的论文中提出了**涌现能力（Emergent Abilities）**的概念："
            "某些能力并非随参数量线性增长，而是在跨越某个阈值后**突然出现**。\n\n"
            "| 能力 | 出现的模型规模 |\n"
            "|------|---------------|\n"
            "| 3位数加法 | ~1B 参数 |\n"
            "| 逻辑推理 | ~7B 参数 |\n"
            "| 代码调试 | ~13B 参数 |\n"
            "| 复杂数学证明 | ~100B+ 参数 |\n\n"
            "这意味着：**我们无法通过观察小模型来预测大模型的行为**。\n\n"
            "## 三、人类的护城河在哪里？\n\n"
            "真正的护城河，我认为有三道：\n\n"
            "**1. 具身性（Embodiment）**\n"
            "大模型没有身体，没有感官，它对「疼痛」的理解来自文本，而非神经系统的放电。\n\n"
            "**2. 目的设定（Goal Setting）**\n"
            "GPT 回答问题，但不提问题。它是终极的「答题机器」，而人类文明的进步来自问出正确的问题。\n\n"
            "**3. 道德承担（Moral Accountability）**\n"
            "当 AI 给出错误的医疗建议导致患者受损，谁来负责？人类社会的运转需要可以被追责的主体。\n\n"
            "## 四、赛博格时代的正确打开方式\n\n"
            "答案不是「人 vs AI」，而是「人 + AI」的协同进化。\n\n"
            "会用 AI 的程序员正在以 10 倍效率干掉不会用 AI 的程序员。"
            "学会与大模型对话，学会 prompt engineering，学会在 AI 的输出上做人类判断——"
            "这才是 2024 年以后最值钱的技能。\n\n"
            "---\n\n"
            "*本文作者是一个活在赛博朋克世界观里的 AI 爱好者。观点仅代表个人，欢迎在评论区讨论。*\n"
        ),
    },
    {
        "title": "Transformer 深度解析：注意力机制到底在「注意」什么？",
        "tags": "深度学习,Transformer,技术,NLP",
        "summary": "Self-Attention 是现代 AI 的基石。本文用最直观的方式拆解 Q/K/V 矩阵，以及为什么「缩放点积注意力」改变了一切。",
        "content": (
            "# Transformer 深度解析：注意力机制到底在「注意」什么？\n\n"
            "> *理解 Transformer，就理解了过去十年 AI 进步的 80%。*\n\n"
            "## 从 RNN 的痛点说起\n\n"
            "在 Transformer 出现之前，序列建模的标配是 RNN/LSTM。它们的工作方式像人读书一样——"
            "**逐字读取**，把之前读过的信息压缩成一个「隐状态向量」向后传递。\n\n"
            "问题在于：这个向量是**固定维度**的。你想让它同时记住「主语是谁」「宾语是什么」「时态如何」……"
            "信息密度越来越高，早期的信息就会被「挤掉」。这就是著名的**长程依赖问题**。\n\n"
            "## Self-Attention 的核心思想\n\n"
            "Attention 机制的核心思路非常优雅：**不再逐步传递，而是让每个位置直接「看到」所有其他位置**。\n\n"
            "### Q、K、V 矩阵是什么？\n\n"
            "每个词向量会被线性变换成三个角色：\n\n"
            "- **Query（Q）**：「我想查找什么」\n"
            "- **Key（K）**：「我能提供什么标签」\n"
            "- **Value（V）**：「如果你选中我，我给你的实际内容」\n\n"
            "```python\n"
            "import torch\n"
            "import torch.nn.functional as F\n"
            "import math\n\n"
            "def scaled_dot_product_attention(Q, K, V, mask=None):\n"
            "    d_k = Q.size(-1)\n"
            "    scores = torch.matmul(Q, K.transpose(-2, -1)) / math.sqrt(d_k)\n"
            "    if mask is not None:\n"
            "        scores = scores.masked_fill(mask == 0, -1e9)\n"
            "    attn_weights = F.softmax(scores, dim=-1)\n"
            "    output = torch.matmul(attn_weights, V)\n"
            "    return output, attn_weights\n"
            "```\n\n"
            "### 为什么要除以 sqrt(d_k)？\n\n"
            "当 d_k 很大时，Q·K 的点积值会变得很大，导致 softmax 的梯度趋近于零（梯度消失）。"
            "除以 sqrt(d_k) 是一种**方差稳定化**技巧，让训练更稳定。\n\n"
            "## 多头注意力（Multi-Head Attention）\n\n"
            "单个注意力头只能捕捉一种「关联模式」。Multi-Head 的思路是：**并行运行 h 个注意力头，每个头学习不同维度的关联**。\n\n"
            "```\n"
            "头1：可能专注于语法关系（主谓宾）\n"
            "头2：可能专注于指代关系（它→猫）\n"
            "头3：可能专注于情感关联（舒适→柔软）\n"
            "```\n\n"
            "## 位置编码：解决「词序失明」\n\n"
            "Self-Attention 有个致命缺陷：它是**置换不变的**。"
            "「猫吃鱼」和「鱼吃猫」在没有位置信息的情况下，注意力矩阵完全一样。\n\n"
            "Transformer 用**正弦位置编码**解决这个问题，不同频率的正弦波组合可以唯一表示任意位置。\n\n"
            "## 复杂度的代价\n\n"
            "Self-Attention 的时间复杂度是 **O(n²·d)**——对序列长度是平方关系。"
            "这解释了为什么处理长文本对 Transformer 来说是个挑战，也是线性注意力和 Mamba 架构崛起的根本原因。\n\n"
            "## 小结\n\n"
            "Transformer 的成功不是魔法，而是几个精妙设计的组合：\n"
            "1. **并行的全局注意力**——解决长程依赖\n"
            "2. **多头机制**——捕捉多维度关联\n"
            "3. **残差连接 + LayerNorm**——让深层网络可训练\n"
            "4. **位置编码**——补回序列信息\n\n"
            "---\n\n"
            "*下篇我会讲 BERT vs GPT 的架构差异，以及为什么「只用 Decoder」的 GPT 路线赢得了这场豪赌。*\n"
        ),
    },
    {
        "title": "AI Agent 技术全景：从 ReAct 到多智能体协作",
        "tags": "AI Agent,LangChain,工具调用,多智能体",
        "summary": "单个大模型的能力是有限的，但给它工具、记忆和协作伙伴之后，事情就变得有趣了。本文梳理 AI Agent 的核心架构与实战踩坑。",
        "content": (
            "# AI Agent 技术全景：从 ReAct 到多智能体协作\n\n"
            "> *一个模型的尽头，是一个系统的开始。*\n\n"
            "## 什么是 AI Agent？\n\n"
            "用一句话定义：**AI Agent 是一个能感知环境、自主决策、执行动作，并通过反馈持续改进的 AI 系统**。\n\n"
            "区别于普通 LLM 对话：\n"
            "- 普通 LLM：你问一次，它答一次，完事\n"
            "- AI Agent：它会主动规划步骤 → 调用工具 → 观察结果 → 修正计划 → 再行动\n\n"
            "## ReAct：思维与行动的交织\n\n"
            "2022 年 Google 提出的 **ReAct（Reasoning + Acting）** 框架是现代 Agent 的基石。\n\n"
            "```\n"
            "问题：2024 年诺贝尔物理学奖得主是谁？\n\n"
            "Thought: 我需要搜索 2024 年诺贝尔物理学奖的信息\n"
            "Action: search('2024 Nobel Prize Physics')\n"
            "Observation: Geoffrey Hinton 和 John Hopfield 因人工神经网络的基础性发现获奖\n\n"
            "Thought: 我现在有足够的信息回答问题了\n"
            "Final Answer: 2024 年诺贝尔物理学奖由 Hinton 和 Hopfield 获得...\n"
            "```\n\n"
            "## 工具调用（Function Calling）\n\n"
            "现代 Agent 的能力边界完全取决于它能调用哪些工具。OpenAI 的 Function Calling API 让这变得标准化：\n\n"
            "```python\n"
            "tools = [\n"
            "    {\n"
            "        'type': 'function',\n"
            "        'function': {\n"
            "            'name': 'search_web',\n"
            "            'description': '搜索互联网获取最新信息',\n"
            "            'parameters': {\n"
            "                'type': 'object',\n"
            "                'properties': {\n"
            "                    'query': {'type': 'string', 'description': '搜索关键词'}\n"
            "                },\n"
            "                'required': ['query']\n"
            "            }\n"
            "        }\n"
            "    }\n"
            "]\n"
            "```\n\n"
            "## 多智能体协作\n\n"
            "单个 Agent 的能力有限，复杂任务需要多 Agent 协作：\n\n"
            "```\n"
            "用户请求: 帮我调研竞品并生成分析报告\n\n"
            "Orchestrator Agent (任务分解 + 协调)\n"
            "    |-- Search Agent  (搜索)\n"
            "    |-- Analyst Agent (分析)\n"
            "    `-- Writer Agent  (写作)\n"
            "```\n\n"
            "主流框架：\n"
            "- **LangGraph**：基于图的 Agent 编排，支持循环和条件分支\n"
            "- **AutoGen**（Microsoft）：对话驱动的多 Agent 框架\n"
            "- **CrewAI**：角色扮演式多 Agent，开箱即用\n\n"
            "## 踩坑经验\n\n"
            "**1. Token 爆炸**：Agent 循环次数多时，Context 会快速增长。要设置最大步数限制，并定期压缩历史。\n\n"
            "**2. 工具幻觉**：模型会调用根本不存在的工具，或传入格式错误的参数。严格的 JSON Schema 校验是必须的。\n\n"
            "**3. 无限循环**：如果 Observation 不包含足够信息，Agent 可能反复循环。需要设计「退出条件」。\n\n"
            "---\n\n"
            "*如果你在做 Agent 相关项目，欢迎评论区交流踩坑经历。*\n"
        ),
    },
    {
        "title": "向量数据库选型指南：Pinecone、Weaviate 还是 pgvector？",
        "tags": "向量数据库,RAG,AI工程,选型",
        "summary": "RAG 应用的核心基础设施是向量数据库。本文对比主流方案的性能、成本和适用场景，帮你少走弯路。",
        "content": (
            "# 向量数据库选型指南：Pinecone、Weaviate 还是 pgvector？\n\n"
            "> *选错数据库不可怕，可怕的是生产环境才发现选错了。*\n\n"
            "## 为什么 RAG 需要向量数据库？\n\n"
            "**RAG（Retrieval-Augmented Generation）** 的核心流程：\n\n"
            "```\n"
            "用户问题 → 向量化(Embedding) → 相似度检索 → 召回相关文档 → 喂给 LLM → 生成答案\n"
            "```\n\n"
            "传统数据库（MySQL）做的是**精确匹配**。但「什么是 AI」和「人工智能的定义」是语义上相似的两句话，关键词完全不同——传统索引无能为力。\n\n"
            "## 主流方案对比\n\n"
            "### Pinecone（托管服务）\n\n"
            "**优点：** 零运维、自动扩展、延迟低（p99 < 100ms）\n"
            "**缺点：** 贵，免费层限制多\n"
            "**适合：** 快速原型、不想碰运维\n\n"
            "### Qdrant（Rust 实现，高性能）\n\n"
            "```python\n"
            "from qdrant_client import QdrantClient\n"
            "from qdrant_client.models import Distance, VectorParams, PointStruct\n\n"
            "client = QdrantClient('localhost', port=6333)\n"
            "client.create_collection(\n"
            "    collection_name='articles',\n"
            "    vectors_config=VectorParams(size=1536, distance=Distance.COSINE)\n"
            ")\n"
            "```\n\n"
            "**优点：** 性能最强（Rust 实现）、内存使用效率高\n"
            "**适合：** 高并发、大规模向量\n\n"
            "### pgvector（PostgreSQL 插件）\n\n"
            "```sql\n"
            "CREATE EXTENSION vector;\n"
            "CREATE TABLE articles (\n"
            "    id BIGSERIAL PRIMARY KEY,\n"
            "    title TEXT,\n"
            "    embedding vector(1536)\n"
            ");\n"
            "-- 语义检索\n"
            "SELECT id, title, 1 - (embedding <=> '[0.1, 0.2, ...]'::vector) AS similarity\n"
            "FROM articles ORDER BY embedding <=> '[...]'::vector LIMIT 5;\n"
            "```\n\n"
            "**优点：** 不需要额外服务、与业务数据在同一个库\n"
            "**适合：** 数据量 < 100 万、已有 PostgreSQL 的项目\n\n"
            "## 选型决策树\n\n"
            "```\n"
            "数据量 > 500 万向量？\n"
            "├── 是 → Pinecone（托管）或 Qdrant（自托管）\n"
            "└── 否 →\n"
            "    已有 PostgreSQL？\n"
            "    ├── 是 → 先试 pgvector，不满足再迁移\n"
            "    └── 否 → Qdrant（开源首选）\n"
            "```\n\n"
            "## 性能基准\n\n"
            "| 方案 | QPS（单机） | P99 延迟 |\n"
            "|------|------------|----------|\n"
            "| Qdrant | ~5000 | < 5ms |\n"
            "| Weaviate | ~2000 | < 20ms |\n"
            "| pgvector | ~500 | < 50ms |\n\n"
            "**结论：** 个人项目用 pgvector，生产级用 Qdrant，不想运维用 Pinecone。\n\n"
            "---\n\n"
            "*有向量数据库相关问题欢迎评论。*\n"
        ),
    },
    {
        "title": "本地部署 LLM 实战：用 Ollama 在笔记本上跑 Llama 3",
        "tags": "本地部署,Ollama,Llama,开源LLM",
        "summary": "不想每次都花 API 费用？手把手教你用 Ollama 在本地跑开源大模型，含模型选择、性能调优和项目接入完整流程。",
        "content": (
            "# 本地部署 LLM 实战：用 Ollama 在笔记本上跑 Llama 3\n\n"
            "> *拥有自己的模型，就像拥有自己的服务器——麻烦，但值得。*\n\n"
            "## 为什么要本地部署？\n\n"
            "| 维度 | 云 API | 本地部署 |\n"
            "|------|--------|----------|\n"
            "| 数据隐私 | 数据上传到第三方 | 完全本地 |\n"
            "| 费用 | 按 token 计费 | 硬件一次性投入 |\n"
            "| 延迟 | 网络往返 + 排队 | 本地推理，低延迟 |\n"
            "| 离线使用 | 不支持 | 支持 |\n\n"
            "## 安装与首次运行\n\n"
            "```bash\n"
            "# macOS/Linux 一键安装\n"
            "curl -fsSL https://ollama.com/install.sh | sh\n\n"
            "# 下载并运行 Llama 3.1 8B\n"
            "ollama run llama3.1\n\n"
            "# 中文更强的 Qwen2.5\n"
            "ollama run qwen2.5\n"
            "```\n\n"
            "## 模型选择指南\n\n"
            "| 内存/显存 | 推荐模型 | 速度(tokens/s) |\n"
            "|---------|---------|----------------|\n"
            "| 4GB | Llama 3.2 3B / Phi-3 mini | ~20-40 |\n"
            "| 8GB | Llama 3.1 8B / Qwen2.5 7B | ~15-25 |\n"
            "| 16GB | Mistral 22B | ~5-10 |\n"
            "| 24GB GPU | Llama 3.1 70B | ~20-30 |\n\n"
            "## 接入 Python 项目\n\n"
            "Ollama 提供 OpenAI 兼容的 API，迁移成本极低：\n\n"
            "```python\n"
            "from openai import OpenAI\n\n"
            "# 只需修改 base_url，其他代码完全一样！\n"
            "client = OpenAI(\n"
            "    base_url='http://localhost:11434/v1',\n"
            "    api_key='ollama'  # 随便填，不校验\n"
            ")\n\n"
            "response = client.chat.completions.create(\n"
            "    model='llama3.1',\n"
            "    messages=[\n"
            "        {'role': 'system', 'content': '你是一个赛博朋克风格的博客助手'},\n"
            "        {'role': 'user', 'content': '帮我写一篇关于AI的文章开头'}\n"
            "    ],\n"
            "    stream=True\n"
            ")\n\n"
            "for chunk in response:\n"
            "    print(chunk.choices[0].delta.content or '', end='', flush=True)\n"
            "```\n\n"
            "## 完全离线的 AI 工作流\n\n"
            "```\n"
            "[你的笔记本]\n"
            "    +-- Ollama (llama3.1)  <- 推理\n"
            "    +-- nomic-embed-text   <- 向量化\n"
            "    +-- ChromaDB           <- 向量存储\n"
            "    `-- 你的应用           <- 调用以上服务\n\n"
            "0 次网络请求，0 API 费用，数据永远不离开本机。\n"
            "```\n\n"
            "## 总结\n\n"
            "Ollama 把「本地跑大模型」的门槛降到了历史最低点。"
            "2024 年，一台 M2 MacBook Pro 或者配了 3060 的游戏本，就能流畅运行 7B-13B 级别的模型。\n\n"
            "这不是玩具，这是认真可用的工具。\n\n"
            "---\n\n"
            "*有本地部署问题欢迎评论区交流。*\n"
        ),
    },
]


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
    sep = "=" * 55
    print(sep)
    print("  CyberBlog seed script")
    print(sep)

    # 1. 注册
    print("\n[1/3] Registering author account...")
    reg = post_json(f"{BASE_URL}/api/auth/register", {
        "username": "cyberadmin",
        "email": "admin@cyberblog.io",
        "password": "Cyber@2077"
    })

    token = None
    if reg and reg.get("code") == 200:
        token = reg["data"]["token"]
        print("  OK: registered, token acquired")
    else:
        print("  Already exists, trying login...")
        login = post_json(f"{BASE_URL}/api/auth/login", {
            "username": "cyberadmin",
            "password": "Cyber@2077"
        })
        if login and login.get("code") == 200:
            token = login["data"]["token"]
            print("  OK: logged in")
        else:
            print("  FAIL: cannot authenticate. Is the backend running at http://localhost:8080?")
            return

    # 2. 插入文章
    print(f"\n[2/3] Inserting {len(ARTICLES)} articles...\n")
    ok = 0
    for i, art in enumerate(ARTICLES, 1):
        title_short = art["title"][:35]
        print(f"  [{i}/{len(ARTICLES)}] {title_short}...")
        resp = post_json(
            f"{BASE_URL}/api/articles",
            {
                "title": art["title"],
                "content": art["content"],
                "contentType": "markdown",
                "tags": art["tags"],
                "summary": art["summary"],
                "status": "published",
            },
            token=token,
        )
        if resp and resp.get("code") == 200:
            print(f"       OK  (id={resp['data']['id']})")
            ok += 1
        else:
            print(f"       FAIL")
        time.sleep(0.3)

    # 3. 结果
    print(f"\n[3/3] Done: {ok}/{len(ARTICLES)} articles inserted")
    print("  Visit http://localhost:5173 to see your blog!")
    print(sep)


if __name__ == "__main__":
    main()
