/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations;

import java.util.List;

public interface DefaultValueBridgeExpectations<V, F> {
	String TYPE_WITH_VALUE_BRIDGE_1_NAME = "TypeWithValueBridge1Name";
	String TYPE_WITH_VALUE_BRIDGE_2_NAME = "TypeWithValueBridge2Name";
	String TYPE_WITH_VALUE_BRIDGE_FIELD_NAME = "myProperty";
	String TYPE_WITH_VALUE_BRIDGE_FIELD_INDEXNULLAS_NAME = "indexNullAsProperty";

	/**
	 * @return The type returned by projections. For primitive types, this is the corresponding boxed type.
	 * For other types, this is the type itself.
	 */
	Class<V> getProjectionType();

	Class<F> getIndexFieldJavaType();

	List<V> getEntityPropertyValues();

	default List<V> getProjectionValues() {
		return getEntityPropertyValues();
	}

	List<F> getDocumentFieldValues();

	Class<?> getTypeWithValueBridge1();

	Object instantiateTypeWithValueBridge1(int identifier, V propertyValue);

	Class<?> getTypeWithValueBridge2();

	F getNullAsValueBridge1();

	F getNullAsValueBridge2();

	default String getUnparsableNullAsValue() {
		return "---";
	}
}