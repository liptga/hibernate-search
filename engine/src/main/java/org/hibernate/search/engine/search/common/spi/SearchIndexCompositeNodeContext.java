/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.common.spi;

/**
 * Information about a composite index element targeted by search; either the index root or an object field.
 *
 * @param <SC> The type of the backend-specific search scope.
 */
public interface SearchIndexCompositeNodeContext<SC extends SearchIndexScope<SC>>
		extends SearchIndexNodeContext<SC> {

	String absolutePath(String relativeFieldName);

	SearchIndexCompositeNodeTypeContext<SC, ?> type();

}