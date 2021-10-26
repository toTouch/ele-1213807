# Elasticsearch

 ## 概念

简称ES，开源的高扩展的分布式全文搜索引擎。 

## 启动

在bin目录下直接启动

***注意：***

​    9300端口是Elasticsearch集群间组件的通信端口，9200端口为浏览器的访问的http协议的restful端口。

## 倒排索引

es是面向文档型数据库，一条数据在这里就是一个文档。

***index*** 可以类比关系型数据库的一个库，***Types***相当于表，***Documents***相当于表的行,***Fields***相当于列。***Type***在7.X版本，已经被删除了。

## ES基础操作

### 创建索引

PUT   http://xxxx:9200/shopping

返回

```json
{
    "acknowledged": true,
    "shards_acknowledged": true,
    "index": "shopping"
}
```

### 查询索引

GET http://xxxx:9200/shopping

返回：

```json
{
    "shopping": {
        "aliases": {},
        "mappings": {},
        "settings": {
            "index": {
                "routing": {
                    "allocation": {
                        "include": {
                            "_tier_preference": "data_content"
                        }
                    }
                },
                "number_of_shards": "1",
                "provided_name": "shopping",
                "creation_date": "1631028183902",
                "number_of_replicas": "1",
                "uuid": "Z1wX0hXbSs-JYKGbLTOjQw",
                "version": {
                    "created": "7140199"
                }
            }
        }
    }
}
```

#### 查询所有索引

GET http://localhost:9200/_cat/indices?v

```
health status index     uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   shopping2 bYJOS12JT7-Odg6YBKkKtg   1   1          0            0       208b           208b
yellow open   shopping  Z1wX0hXbSs-JYKGbLTOjQw   1   1          0            0       208b           208b

```

### 删除索引

DELETE http://xxxx:9200/shopping

```json
{
    "acknowledged": true
}
```

### 添加文档

POST  http://localhost:9200/shopping/_doc

***body***:  

```json
{"name":"woshini","age":1}
```

返回：

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "5xHMynsBHMceEgEMKHZB",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 2
} 
```

POST  http://localhost:9200/shopping/_doc/{自定义的id}

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1111",
    "_version": 2,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 2,
    "_primary_term": 2
}
```

POST  http://localhost:9200/shopping/_create/{自定义的id}

这种方式就是单纯的创建，如果重复传id，就会报错

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1113",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 4,
    "_primary_term": 2
}
```

### 查询文档（根据主键）

GET http://localhost:9200/shopping/_doc/1113

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1113",
    "_version": 1,
    "_seq_no": 4,
    "_primary_term": 2,
    "found": true,
    "_source": {
        "name": "woshini",
        "age": 1
    }
}
```

#### 查询索引下所有的数据

GET  http://localhost:9200/shopping/_search

```json
{
    "took": 753,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 4,
            "relation": "eq"
        },
        "max_score": 1.0,
        "hits": [
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "5xHMynsBHMceEgEMKHZB",
                "_score": 1.0,
                "_source": {
                    "name": "woshini",
                    "age": 1
                }
            },
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "1111",
                "_score": 1.0,
                "_source": {
                    "name": "woshini",
                    "age": 1
                }
            },
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "1112",
                "_score": 1.0,
                "_source": {
                    "name": "woshini",
                    "age": 1
                }
            },
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "1113",
                "_score": 1.0,
                "_source": {
                    "name": "woshini",
                    "age": 1
                }
            }
        ]
    }
}
```

### 修改文档

PUT http://localhost:9200/shopping/_doc/{id}   这是种全量更新，就是整个json会被替换，必须要put，而且版本号会修改

请求体：

```json
{"name":"woshini","age":2}
```

响应：

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1113",
    "_version": 2,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 5,
    "_primary_term": 2
}
```

POST  http://localhost:9200/shopping/_update/1113  局部更新,会更新已存在的字段，不会全量替换字段，也会增加新的字段，版本号不会修改

请求体：要加doc，否则会报错

 ```json
 {
     "doc":{
         "title":"huawe",
         "age"
     }
 }
 ```



```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1113",
    "_version": 6,
    "result": "noop",
    "_shards": {
        "total": 0,
        "successful": 0,
        "failed": 0
    },
    "_seq_no": 9,
    "_primary_term": 2
}
```

 ### 删除文档

DELETE http://localhost:9200/shopping/_doc/1113

```json
{
    "_index": "shopping",
    "_type": "_doc",
    "_id": "1113",
    "_version": 8,
    "result": "deleted",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 11,
    "_primary_term": 2
}
```

### 文档条件查询

GET   http://localhost:9200/shopping/_search

推荐使用请求体json来写查询条件

+ 例子1,查询name=123的

  ```json
  {"query":{
      "match":{
          "name":"123"
      }
  }}
  ```

+ 例子2，查询所有的数据:

  ```json
  {"query":{
      "match_all":{}
  }}
  ```

+ 例子3，分页查询

  ```json
  {"query":{
      "match_all":{}
  },
  "from":0,
  "size":1
  }
  ```

+ 例子4，过滤结果字段

```json
{"query":{
    "match_all":{}
},
"from":0,
"size":1,
"_source":["age"]
}
```

+ 例子5，对结果排序

```json
{"query":{
    "match_all":{}
},
"from":0,
"size":2,
"_source":["age"],
"sort":{
    "age":{
        "order":"asc"
    }
}
}
```

+ 例子6，多个条件组合，bool必须有，表示条件查询，must表示and， should表示or

```json
{
	"query":{
	    "bool":{//条件
	         "must" : [
	             {"match":{
	                "name":"123"
	             }},
	             {"match":{
	                "age":1
	             }}
	          ]
	    }
	}
}



{
	"query":{
	    "bool":{//条件
	         "should" : [
	             {"match":{
	                "name":"123"
	             }},
	             {"match":{
	                "age":1
	             }}
	          ]
	    }
	}
}
```

+ 例子，范围查询

```json
{
	"query":{
	    "bool":{//条件
	         "should" : [
	             {"match":{
	                "name":"123"
	             }},
	             {"match":{
	                "age":1
	             }}
	          ],
              "filter":{
                  "range": {
                      "age":{
                          "gt":0
                      }
                  }
              }

    
	    }
	}
}
```

### 文档匹配查询

GET http://localhost:9200/shopping/_search

#### 模糊匹配

请求体：

 ```json
 {
     "query":{
         "match":{
             "name" :"jordan farron"
         }
     }
 }
 ```

#### 完全匹配

请求体：

```json
{
    "query":{
        "match_phrase":{
            "name" :"1farron"
        }
    }
}
```

#### 高亮显示

```json
{
    "query":{
        "match":{
            "name" :"jordan farron"
        }
    },
    "highlight":{
        "fields":{
            "name" :{}
        }
    }
}
```

返回：

```json
{
    "took": 121,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 1,
            "relation": "eq"
        },
        "max_score": 1.0892314,
        "hits": [
            {
                "_index": "shopping",
                "_type": "_doc",
                "_id": "1113",
                "_score": 1.0892314,
                "_source": {
                    "name": "Eclar farron",
                    "age": 1
                },
                "highlight": {
                    "name": [
                        "Eclar <em>farron</em>"
                    ]
                }
            }
        ]
    }
}
```

### 文档聚合查询

GET  http://localhost:9200/shopping/_search

请求体：

```json
{
  "aggs":{//聚合操作
      "age_group":{ //统计名称，名字随意
            "terms":{//分组
                "field":"age"
            }
      }
  },
  "size":0//不显示原始数据
}
```

```json
{
    "took": 11,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 4,
            "relation": "eq"
        },
        "max_score": null,
        "hits": []
    },
    "aggregations": {
        "age_group": {
            "doc_count_error_upper_bound": 0,
            "sum_other_doc_count": 0,
            "buckets": [
                {
                    "key": 1,
                    "doc_count": 4
                }
            ]
        }
    }
}
```

#### 求平均值

```json
{
  "aggs":{//聚合操作
      "age_group":{ //统计名称，名字随意
            "avg":{//平均值
                "field":"age"
            }
      }
  },
  "size":0
}
```

返回：

```json
{
    "took": 24,
    "timed_out": false,
    "_shards": {
        "total": 1,
        "successful": 1,
        "skipped": 0,
        "failed": 0
    },
    "hits": {
        "total": {
            "value": 4,
            "relation": "eq"
        },
        "max_score": null,
        "hits": []
    },
    "aggregations": {
        "age_group": {
            "value": 1.0
        }
    }
}
```

### 设置索引的属性（映射）

可以为索引中的文档哪些字段要分词，哪些不需要分词，哪些需要索引，哪些不需要索引,那么接下来对这些属性的操作要按照规则来。

```json
{
    "properties":{
        "name":{
            "type":"text",//是否分词 text表示分词 ，keword表示不分词
            "index":true//是否索引查询

        },

        "sex":{
            "type":"keyword",
            "index":true
        },
        "tel":{
            "type":"keyword",
            "index":false
        }
    }
}
```



 

