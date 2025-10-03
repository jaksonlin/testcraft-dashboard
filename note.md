# note

## migrate

```bash
mvn -Dflyway.url=jdbc:postgresql://127.0.0.1:5432/test_analytics -Dflyway.user=postgres -Dflyway.password=postgres flyway:migrate    

mvn -Dflyway.url=jdbc:postgresql://127.0.0.1:5432/test_analytics_shadow -Dflyway.user=postgres -Dflyway.password=postgres flyway:migrate   

mvn -Dflyway.url=jdbc:postgresql://127.0.0.1:5432/test_analytics_test -Dflyway.user=postgres -Dflyway.password=postgres flyway:migrate   
```

## test


## run

shadow write
``` bash
java -Dhex.write.shadow=true -jar target/annotation-extractor-1.0.0.jar ./repos ./repo-list.txt --db-host localhost --db-name primary_db --shadow-db-host localhost --shadow-db-name primary_db_shadow
```

hex write
``` bash
java -Dhex.write.enabled=true -jar target/annotation-extractor-1.0.0.jar
```

hex read
``` bash
java -Dhex.read.enabled=true -jar target/annotation-extractor-1.0.0.jar
```