
# Alibaba langengine: an AI application development framework written in Java.

<h4 align="center">

<div align="center">
<img src="https://img.shields.io/badge/Version-1.0.0-blue.svg" alt="Version"> 
<img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License">
<img src="https://img.shields.io/github/stars/AIDC-AI/ali-langengine?color=yellow" alt="Stars">
<img src="https://img.shields.io/github/issues/AIDC-AI/ali-langengine?color=red" alt="Issues">
<img src="https://img.shields.io/badge/Java-000000?logo=OpenJDK" alt="Java">
</div>
</h4>

**Alibaba langengine**

阿里巴巴langengine是一个基于Java的AI应用开发框架。它赋予LLM两大核心能力：

1. 数据感知，将语言模型与其他数据源相连接；

2. 代理能力，允许语言模型与工程系统化能力互动；

alibaba-langengine的主要应用场景包括个人助手、基于文档的问答、聊天机器人、查询表格数据、代码分析、低代码应用生成等。

**相关代码**

alibaba-langengine-core：最核心的AI应用框架引擎模块。

alibaba-langengine-infrastructure：AI应用框架基础设施模块。

alibaba-langengine-community：社区开源共建模块。

alibaba-langengine-demo：相关示例模块。

**JDK版本要求**

JDK 8+

**相关配置**

alibaba-langengine-openai
```properties
openai_server_url=https://api.openai.com/
openai_api_key=******
openai_api_timeout=100
# 兼容
OPENAI_API_KEY=******
```

alibaba-langengine-dashscope
```properties
# dashscope api
dashscope_server_url=https://dashscope.aliyuncs.com/
dashscope_api_key=******
dashscope_api_timeout=100
# 兼容
DASH_SCOPE_API=******
```

alibaba-langengine-tool
```properties
# bing api
bing_server_url=https://api.bing.microsoft.com/
bing_api_key=******

# google api
google_customsearch_server_url=https://customsearch.googleapis.com/
google_api_key=******
google_cse_id=******

# serpapi api
serpapi_server_url=https://serpapi.com/
serpapi_key=******

# tavily api
tavily_api_key=******
```

alibaba-langengine-adbpg
```properties
# adbpg db
adbpg_datasource_endpoint=******
adbpg_datasource_databasename=******
adbpg_datasource_u=******
adbpg_datasource_p=******
```

alibaba-langengine-azure
```properties
# azure api
azure_openai_server_url=******
azure_deployment_name=******
azure_openai_api_version=******
azure_openai_api_timeout=100
```

alibaba-langengine-claude
```properties
# claude api
anthropic_server_url=https://api.anthropic.com/
anthropic_api_key=******
anthropic_api_timeout=120
```

alibaba-langengine-gemini
```properties
# gemini api
gemini_api_key=******
gemini_api_timeout=120
```

alibaba-langengine-hologres
```properties
# hologres db
hologres_datasource_endpoint=******
hologres_datasource_databasename=knowledge_center
hologres_datasource_u=******
hologres_datasource_p=******
```

alibaba-langengine-huggingface
```properties
# huggingface api
huggingface_api_key=******
```

alibaba-langengine-milvus
```properties
# milvus
milvus_server_url=******
```

alibaba-langengine-minimax
```properties
#minimax api
minimax_api_key=******
minimax_group_id=******
minimax_api_timeout=120
```

alibaba-langengine-moonshot
```properties
# moonshot api
moonshot_server_url=https://api.moonshot.cn
moonshot_api_key=******
moonshot_api_version=v1
moonshot_server_timeout=120
```

alibaba-langengine-msearch
```properties
#msearch api
msearch_api_key=******
msearch_api_timeout=120
```

alibaba-langengine-opensearch
```properties
# opensearch vector
opensearch_datasource_instance_id=ha-cn-*****
opensearch_datasource_endpoint=ha-cn-*****
opensearch_datasource_swift_server_root=http://*****
opensearch_datasource_swift_topic=ha-cn-******
```

alibaba-langengine-pinecone
```properties
# pinecone vector
pinecone_api_key=******
pinecone_environment=us-west4-gcp-free
pinecone_project_name=******
```

alibaba-langengine-polardb
```properties
# polardb postgres
polardb_datasource_endpoint=******
polardb_datasource_databasename=******
polardb_datasource_u=******
polardb_datasource_p=******
```

alibaba-langengine-redis
```properties
# redis
redis_host=r-******.redis.rds.aliyuncs.com
redis_port=6379
redis_p=******
redis_session_expire_second=60
```

alibaba-langengine-tair
```properties
# tair vector
tair_host=r-******.redis.rds.aliyuncs.com
tair_port=6379
tair_p=******
```

alibaba-langengine-vertexai
```properties
# vertexai
vertexai_server_url=https://us-central1-aiplatform.googleapis.com/
vertexai_api_key=******
vertexai_project_id=******
vertexai_api_timeout=120
```

alibaba-langengine-xingchen
```properties
# xingchen
xingchen_api_key=lm-******
xingchen_api_timeout=120
```

alibaba-langengine-xinghuo
```properties
# xinghuo
xinghuo_server_url=https://spark-api.xf-yun.com/v2.1/chat
xinghuo_app_id=******
xinghuo_api_key=******
xinghuo_api_secret=******
```

**License**

This project is licensed under Apache License Version 2 ([https://www.apache.org/licenses/LICENSE-2.0.txt](https://www.apache.org/licenses/LICENSE-2.0.txt), SPDX-License-identifier: Apache-2.0).