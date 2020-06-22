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
package ma.markware.charybdis.model.field.entry;

public class MapEntry<T> implements EntryExpression<T> {

  private final T key;

  public MapEntry(T key) {
    this.key = key;
  }

  @Override
  public T getKey() {
    return key;
  }

  @Override
  public String getName() {
    return key.toString();
  }
}
