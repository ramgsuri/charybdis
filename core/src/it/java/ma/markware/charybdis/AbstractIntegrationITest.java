/*
 * Charybdis - Cassandra ORM framework
 *
 * Copyright (C) 2020 Charybdis authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ma.markware.charybdis;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import java.util.Map;
import ma.markware.charybdis.test.tools.DatabaseSetupExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(DatabaseSetupExtension.class)
public class AbstractIntegrationITest {

  private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationITest.class);

  @BeforeAll
  static void initDb(CqlSession session) {
    logger.info("Start creating Keyspaces/Udts/Tables");
    session.execute("CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
    session.execute("CREATE TYPE IF NOT EXISTS test_keyspace.test_extra_udt ("
                        + "intvalue int, "
                        + "doublevalue double);");
    session.execute("CREATE TYPE IF NOT EXISTS test_keyspace.test_nested_udt ("
                        + "name text, "
                        + "value text, "
                        + "numbers list<int>);");
    session.execute("CREATE TYPE IF NOT EXISTS test_keyspace.test_udt ("
                        + "udtnested frozen<test_keyspace.test_nested_udt>, "
                        + "number int, "
                        + "value text, "
                        + "udtnestedlist list<frozen<test_keyspace.test_nested_udt>>, "
                        + "udtnestednestedset set<frozen<list<test_keyspace.test_nested_udt>>>, "
                        + "udtnestedmap map<text, frozen<list<test_keyspace.test_nested_udt>>>);");
    session.execute("CREATE TABLE IF NOT EXISTS test_keyspace.test_entity ("
                        + "id uuid, "
                        + "date timestamp, "
                        + "udt frozen<test_keyspace.test_udt>, "
                        + "list frozen<list<text>>, "
                        + "se set<int>, "
                        + "map map<text,text>, "
                        + "nestedlist frozen<list<list<int>>>, "
                        + "nestedset set<frozen<list<int>>>, "
                        + "nestedmap map<text,frozen<map<int,text>>>, "
                        + "enumvalue text, enumlist list<text>, "
                        + "enummap map<int,text>, "
                        + "enumnestedlist list<frozen<set<text>>>, "
                        + "extraudt test_keyspace.test_extra_udt, "
                        + "udtlist list<frozen<test_keyspace.test_udt>>, "
                        + "udtset set<frozen<test_keyspace.test_udt>>, "
                        + "udtmap map<int,frozen<test_keyspace.test_udt>>, "
                        + "udtnestedlist list<frozen<list<test_keyspace.test_udt>>>, "
                        + "flag boolean, "
                        + "creation_date timestamp, "
                        + "last_updated_date timestamp, "
                        + "PRIMARY KEY ((id), date, udt, list));");
    logger.info("End creating Keyspaces/Udts/Tables");
  }

  protected void insertRow(CqlSession session, String keyspaceName, String tableName, Map<String, Term> values) {
    session.execute(QueryBuilder.insertInto(keyspaceName, tableName).values(values).build());
  }

  protected void insertRow(CqlSession session, String keyspaceName, String tableName, Map<String, Term> values, int ttlInSeconds) {
    session.execute(QueryBuilder.insertInto(keyspaceName, tableName).values(values).usingTtl(ttlInSeconds).build());
  }

  protected void insertRow(CqlSession session, String keyspaceName, String tableName, Map<String, Term> values, long timestamp) {
    session.execute(QueryBuilder.insertInto(keyspaceName, tableName).values(values).usingTimestamp(timestamp).build());
  }

  protected void cleanDatabase(CqlSession session) {
    logger.info("Cleaning database");
    session.execute(SimpleStatement.builder("TRUNCATE test_keyspace.test_entity;")
                                   .build());
  }
}
