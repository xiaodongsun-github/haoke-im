## MongoDB 使用简记

存储的数据结构：
```json
# document
{
    "id": 1,
    "name": "zhangsan",
    "age":21,
    "address":"上海"
}
```

## 通过docker安装MongoDB
```shell
# 拉去镜像
docker pull mongo:4.0.3

# 创建容器
docker create --name mongodb -p 27017:27017 -v /data/monogodb:/data/db mongo:4.0.3

# 启动容器
docker start mongodb

# 进入容器
docker exec -it mongodb /bin/bash

# 使用MongoDB客户端进行操作
mongo
# 查询所有的数据库
> show dbs
admin   0.000GB
config  0.000GB
local   0.000GB
# 通过use关键字切换数据库
> use admin
switched to db admin
# 创建数据库
# 说明：在MongoDB中，数据库是自动创建的，通过use切换到新数据库中，进行插入数据即可自动创建数据库
> use testDb
switched to db testDB
> show dbs # 并没有数据库
admin   0.000GB
config  0.000GB
local   0.000GB
> db.user.insert({id:1,name:'zhangsan'}) # 插入数据
WriteResult({ "nInserted" : 1 })
> show dbs
admin   0.000GB
config  0.000GB
local   0.000GB
testDB  0.000GB
# 查看表
> show tables;
user
> show collections
user
# 删除表
> db.user.drop()
true
# 删除数据库
> db.dropDatabase()
{ "dropped" : "testDb", "ok" : 1 }
# 插入数据
> db.user.insert({id:1, name:'zhangsan',age:20})
WriteResult({ "nInserted" : 1 })
> db.user.save({id:2, name:'lisi',age:22})
WriteResult({ "nInserted" : 1 })
# 查询表所有数据
> db.user.find()
{ "_id" : ObjectId("5c651eb680aee242464c0439"), "id" : 1, "name" : "zhangsan", "age" : 20 }
{ "_id" : ObjectId("5c651eca80aee242464c043a"), "id" : 2, "name" : "lisi", "age" : 22 }
# 更新数据
> db.user.update({id:1}, {$set:{age:24}})
WriteResult({ "nMatched" : 1, "nUpserted" : 0, "nModified" : 1 })
# 不存在的字段更新时，会新增
> db.user.update({id:2},{$set:{sex:1}})
WriteResult({ "nMatched" : 1, "nUpserted" : 0, "nModified" : 1 })
> db.user.find()
{ "_id" : ObjectId("5c651eb680aee242464c0439"), "id" : 1, "name" : "zhangsan", "age" : 24 }
{ "_id" : ObjectId("5c651eca80aee242464c043a"), "id" : 2, "name" : "lisi", "age" : 22, "sex" : 1 }
# 删除数据
# 匹配的全部删除
db.user.remove({age:24})
WriteResult({ "nRemoved" : 1 })
# 只删除匹配的一条数据
> db.user.remove({age:24}, true)
WriteResult({ "nRemoved" : 0 })
# 删除一条
db.user.deleteOne({age:20})
{ "acknowledged" : true, "deletedCount" : 0 }
# 删除一条或者多条
> db.user.deleteMany({age:22})
{ "acknowledged" : true, "deletedCount" : 1 }

# 个数查询
> db.user.find({}, {id:1}).count()
6
# 按条件查询
> db.user.find({id:1}).pretty()
{
    "_id" : ObjectId("5c6526c480aee242464c043b"),
    "id" : 1,
    "name" : "张三",
    "age" : 21
}
# And
db.user.find({age:{$lte:23}, id:{$gte:3}})
{ "_id" : ObjectId("5c65270980aee242464c043d"), "id" : 3, "name" : "王五", "age" : 23 }
{ "_id" : ObjectId("5c6527b080aee242464c0440"), "id" : 6, "name" : "关羽", "age" : 23 }
# Or
> db.user.find({$or:[{id:1}, {id:2}]})
# 跳过几条数据 (分页)
> db.user.find().limit(2)
{ "_id" : ObjectId("5c6526c480aee242464c043b"), "id" : 1, "name" : "张三", "age" : 21 }
{ "_id" : ObjectId("5c6526f980aee242464c043c"), "id" : 2, "name" : "李四", "age" : 22 }
> db.user.find().limit(2).skip(2)
{ "_id" : ObjectId("5c65270980aee242464c043d"), "id" : 3, "name" : "王五", "age" : 23 }
{ "_id" : ObjectId("5c65272a80aee242464c043e"), "id" : 4, "name" : "赵柳", "age" : 24 }
# 排序
> db.user.find().sort({id:-1})
{ "_id" : ObjectId("5c6527b080aee242464c0440"), "id" : 6, "name" : "关羽", "age" : 23 }
{ "_id" : ObjectId("5c65275780aee242464c043f"), "id" : 5, "name" : "赵子龙", "age" : 25 }
{ "_id" : ObjectId("5c65272a80aee242464c043e"), "id" : 4, "name" : "赵柳", "age" : 24 }
{ "_id" : ObjectId("5c65270980aee242464c043d"), "id" : 3, "name" : "王五", "age" : 23 }
# 索引
> db.user.getIndexes()
[
    {
        "v" : 2,
        "key" : {
            "_id" : 1
        },
        "name" : "_id_",
        "ns" : "testdb.user"
    }
]
# 创建
> db.user.createIndex({'age':1})
{
    "createdCollectionAutomatically" : false,
    "numIndexesBefore" : 1,
    "numIndexesAfter" : 2,
    "ok" : 1
}
# 联合索引
> db.user.createIndex({'age':1, 'id':-1})
{
    "createdCollectionAutomatically" : false,
    "numIndexesBefore" : 1,
    "numIndexesAfter" : 2,
    "ok" : 1
}

> db.user.getIndexes()
[
    {
        "v" : 2,
        "key" : {
            "_id" : 1
        },
        "name" : "_id_",
        "ns" : "testdb.user"
    },
    {
        "v" : 2,
        "key" : {
            "age" : 1,
            "id" : -1
        },
        "name" : "age_1_id_-1",
        "ns" : "testdb.user"
    }
]
# 批量插入
> for(var i=1;i<1000;i++)db.user.insert({id:100+i,username:'name_'+i,age:10+i})
WriteResult({ "nInserted" : 1 })

```

mongoDB与SQL的概念对比：

| SQL术语概念 | MongoDB术语概念 | 解释/说明 |
|:---|:----|:--- |
| database | database | 数据库 |
| table | collection | 数据库表/集合 |
| row | document | 数据记录行/文档 |
| column | field | 数据字段/域 |
| index | index | 索引 |
| table join |  | 表连接，MongoDB不支持 | 
| primary key | primary key | 主键，MongoDB自动将_id字段设置为主键 |
