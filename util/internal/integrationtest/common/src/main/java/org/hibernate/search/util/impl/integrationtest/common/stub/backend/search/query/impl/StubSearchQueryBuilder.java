/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.query.impl;

import java.util.concurrent.TimeUnit;

import org.hibernate.search.engine.backend.session.spi.BackendSessionContext;
import org.hibernate.search.engine.search.loading.spi.SearchLoadingContextBuilder;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.spi.SearchQueryBuilder;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl.StubBackend;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.common.impl.StubSearchIndexScope;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.projection.impl.StubSearchProjection;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.projection.impl.StubSearchProjectionContext;

public class StubSearchQueryBuilder<H> implements SearchQueryBuilder<H, StubQueryElementCollector> {

	private final StubBackend backend;
	private final StubSearchIndexScope scope;
	private final StubSearchWork.Builder workBuilder;
	private final StubSearchProjectionContext projectionContext;
	private final SearchLoadingContextBuilder<?, ?, ?> loadingContextBuilder;
	private final StubSearchProjection<H> rootProjection;

	public StubSearchQueryBuilder(StubBackend backend, StubSearchIndexScope scope,
			StubSearchWork.ResultType resultType,
			BackendSessionContext sessionContext,
			SearchLoadingContextBuilder<?, ?, ?> loadingContextBuilder, StubSearchProjection<H> rootProjection) {
		this.backend = backend;
		this.scope = scope;
		this.workBuilder = StubSearchWork.builder( resultType );
		this.projectionContext = new StubSearchProjectionContext( sessionContext );
		this.loadingContextBuilder = loadingContextBuilder;
		this.rootProjection = rootProjection;
	}

	@Override
	public StubQueryElementCollector toQueryElementCollector() {
		return StubQueryElementCollector.get();
	}

	@Override
	public void addRoutingKey(String routingKey) {
		workBuilder.routingKey( routingKey );
	}

	@Override
	public void truncateAfter(long timeout, TimeUnit timeUnit) {
		workBuilder.truncateAfter( timeout, timeUnit );
	}

	@Override
	public void failAfter(long timeout, TimeUnit timeUnit) {
		workBuilder.failAfter( timeout, timeUnit );
	}

	@Override
	public void totalHitCountThreshold(long totalHitCountThreshold) {
		// totalHitCountThreshold is not tested from the mapper
	}

	@Override
	public SearchQuery<H> build() {
		return new StubSearchQuery<>(
				backend, scope.hibernateSearchIndexNames(), workBuilder, projectionContext,
				loadingContextBuilder.build(), rootProjection
		);
	}
}