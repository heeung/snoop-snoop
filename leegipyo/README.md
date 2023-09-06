# TIL_0904

## Apache spark란?
Apache Spark는 데이터 처리와 분석을 위한 오픈 소스 클러스터 컴퓨팅 프레임워크입니다. Spark는 대규모 데이터 집합을 처리하고 분석하는 데 사용되며, 분산 환경에서 빠르고 효율적인 데이터 처리를 지원합니다. 

## pyspark란?
PySpark는 Apache Spark를 Python 프로그래밍 언어로 사용하기 위한 라이브러리입니다. 또한, PySpark는 DataFrame API를 제공하여 구조화된 데이터를 처리하기가 편리합니다. DataFrame은 SQL 스타일의 쿼리와 데이터 조작을 지원하여 데이터 처리 작업을 단순화합니다.

## Pyspark 설치

```pip install pyspark```

## Pyspark 예제

```
from pyspark.sql import SparkSession

# 스파크 세션 생성
spark = SparkSession.builder.master("local").appName("SparkSQL").getOrCreate()
# 로그 레벨 정의
spark.sparkContext.setLogLevel("ERROR")
```

```
# 예제
data = [('001','Smith','M',40,'DA',4000),
        ('002','Rose','M',35,'DA',3000),
        ('003','Williams','M',30,'DE',2500),
        ('004','Anne','F',30,'DE',3000),
        ('005','Mary','F',35,'BE',4000),
        ('006','James','M',30,'FE',3500)]

columns = ["cd","name","gender","age","div","salary"]
df = spark.createDataFrame(data = data, schema = columns)
```

```
>> df.show()
+---+--------+------+---+---+------+
| cd|    name|gender|age|div|salary|
+---+--------+------+---+---+------+
|001|   Smith|     M| 40| DA|  4000|
|002|    Rose|     M| 35| DA|  3000|
|003|Williams|     M| 30| DE|  2500|
|004|    Anne|     F| 30| DE|  3000|
|005|    Mary|     F| 35| BE|  4000|
|006|   James|     M| 30| FE|  3500|
+---+--------+------+---+---+------+
```

# TIL_0905

## Elastic Search Docker 빌드

Elastic Search 설치

```docker pull docker.elastic.co/elasticsearch/elasticsearch:7.9.1```

Elastic Search 실행

```
docker run -d -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name elasticsearch7 docker.elastic.co/elasticsearch/elasticsearch:7.9.1
b5028c898638f74a5aef326899617503b1ef74ee53597cd6c23f50182e4435c6
```

## Elastic Search 사용

Elastic Search 클러스터 연결

```es = Elasticsearch([{'host': 'localhost', 'port': 9200, 'scheme': 'http'}])```

Elastic Search에 데이터 인덱싱

```es.index(index='coupang_data', body=json.dumps(row.to_dict()), headers={"accept": "application/json", "content-type": "application/json"})```

Elastic Search 클러스터 종료

```es.close()```

# TIL_0906

## ELK란 무엇일까?
ELK는 3가지 오픈 소스 소프트웨어인 분석 및 저장 기능을 담당하는 ElasticSearch, 수집 기능을 하는 Logstash, 시각화 도구인 Kibana의 각각의 앞글자를 따서 만든 단어로 각 제품이 연동되어 데이터 수집 및 분석 툴로 활용됩니다.

## Logstash
Logstash는 크게 입력 -> 필터 -> 출력과 같이 3가지 단계로 이루어집니다.

입력에서는 다양한 데이터 저장소로부터 데이터를 입력받는 작업을 진행합니다.

필터에서는 데이터를 확장, 변경, 필터링 및 삭제 처리하는 가공 작업을 진행합니다.

출력에서는 다양한 데이터 저장소로 데이터를 전송하는 작업을 진행합니다.

## ElasticSearch
ElasticSearch는 Lucene 기반으로 개발된 분산 검색 엔진입니다.

ELK 구조에서의 역할은 Logstash를 통해 수신된 데이터를 ElasticSearch에 저장하는 데이터 저장소 역할을 수행합니다.

NoSQL 기반 데이터베이스이며 RDB와 다르게 트랜잭션 Rollback을 지원하지 않으나 검색 및 분석 성능이 뛰어납니다.

## Kibana

Kibana는 사용자에게 분석 결과를 시각화 해주는 소프트웨어로 ElasticSearch에 저장된 데이터들을 시각화하여 차트, 그래프화 또는 로그 데이터를 한번에 모아서 볼 수 있습니다.