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

package ma.markware.charybdis.apt.serializer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import ma.markware.charybdis.apt.AptConfiguration;
import ma.markware.charybdis.apt.AptContext;
import ma.markware.charybdis.apt.AptDefaultConfiguration;
import ma.markware.charybdis.apt.CompilationExtension;
import ma.markware.charybdis.apt.metatype.KeyspaceMetaType;
import ma.markware.charybdis.apt.metatype.TableMetaType;
import ma.markware.charybdis.apt.metatype.UdtMetaType;
import ma.markware.charybdis.model.annotation.Udt;
import ma.markware.charybdis.test.entities.TestEntity;
import ma.markware.charybdis.test.entities.TestExtraUdt;
import ma.markware.charybdis.test.entities.TestKeyspaceDefinition;
import ma.markware.charybdis.test.entities.TestNestedUdt;
import ma.markware.charybdis.test.entities.TestUdt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith({CompilationExtension.class})
class DdlScriptSerializerTest {

  @Mock
  private RoundEnvironment roundEnvironment;
  @Mock
  private Filer filer;

  private AptConfiguration configuration;
  private List<KeyspaceMetaType> keyspaceMetaTypes;
  private List<UdtMetaType> udtMetaTypes;
  private List<TableMetaType> tableMetaTypes;

  @BeforeAll
  void setup(Types types, Elements elements) {
    MockitoAnnotations.initMocks(this);

    TypeElement testNestedUdtElement = elements.getTypeElement(TestNestedUdt.class.getCanonicalName());
    TypeElement testUdtElement = elements.getTypeElement(TestUdt.class.getCanonicalName());
    TypeElement testExtraUdtElement = elements.getTypeElement(TestExtraUdt.class.getCanonicalName());
    Set elementsAnnotatedWithUdt = new HashSet<>(asList(testNestedUdtElement, testUdtElement, testExtraUdtElement));
    when(roundEnvironment.getElementsAnnotatedWith(Udt.class)).thenReturn(elementsAnnotatedWithUdt);

    final AptContext aptContext = new AptContext();
    configuration = AptDefaultConfiguration.initConfig(aptContext, types, elements, filer);
    aptContext.init(roundEnvironment, configuration);

    keyspaceMetaTypes = Collections.singletonList(configuration.getKeyspaceParser()
                 .parse(elements.getTypeElement(TestKeyspaceDefinition.class.getCanonicalName())));
    udtMetaTypes = Arrays.asList(configuration.getUdtParser().parse(testNestedUdtElement), configuration.getUdtParser().parse(testExtraUdtElement),
                                 configuration.getUdtParser().parse(testUdtElement));
    tableMetaTypes = Collections.singletonList(configuration.getTableParser()
                 .parse(elements.getTypeElement(TestEntity.class.getCanonicalName())));
  }

  @Test
  void serializeKeyspaceTest() throws IOException {
    // Given
    StringWriter ddlCreateCqlWriter = new StringWriter();
    StringWriter ddlDropCqlWriter = new StringWriter();
    when(filer.createResource(any(), any(), eq("ddl_create.cql"))).thenReturn(SerializerTestHelper.createJavaFileObject(ddlCreateCqlWriter));
    when(filer.createResource(any(), any(), eq("ddl_drop.cql"))).thenReturn(SerializerTestHelper.createJavaFileObject(ddlDropCqlWriter));

    // When
    configuration.getDdlScriptSerializer().serialize(keyspaceMetaTypes, udtMetaTypes, tableMetaTypes);

    // Then
    InputStream ddlCreateInputStream = getClass().getClassLoader().getResourceAsStream("ddl_create_int.cql");
    assertThat(ddlCreateInputStream).isNotNull();
    String expectedDdlCreateScript = inputStreamToString(ddlCreateInputStream);
    assertThat(expectedDdlCreateScript).isEqualTo(ddlCreateCqlWriter.toString());

    InputStream ddlDropInputStream = getClass().getClassLoader().getResourceAsStream("ddl_drop_int.cql");
    assertThat(ddlDropInputStream).isNotNull();
    String expectedDdlDropScript = inputStreamToString(ddlDropInputStream);
    assertThat(expectedDdlDropScript).isEqualTo(ddlDropCqlWriter.toString());
  }

  private String inputStreamToString(InputStream inputStream) throws IOException {
    StringBuilder textBuilder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader
                                                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
      int c;
      while ((c = reader.read()) != -1) {
        textBuilder.append((char) c);
      }
    }
    return textBuilder.toString();
  }
}
