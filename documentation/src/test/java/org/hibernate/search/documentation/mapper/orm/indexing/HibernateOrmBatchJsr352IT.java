/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.documentation.mapper.orm.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.search.util.impl.integrationtest.mapper.orm.OrmUtils.with;
import static org.junit.Assume.assumeTrue;

import java.util.Properties;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.search.batch.jsr352.core.massindexing.MassIndexingJob;
import org.hibernate.search.documentation.testsupport.BackendConfigurations;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;

import org.junit.Before;
import org.junit.Test;

public class HibernateOrmBatchJsr352IT extends AbstractHibernateOrmMassIndexingIT {

	private static final int JOB_TIMEOUT_MS = 30_000;
	private static final int THREAD_SLEEP = 1000;

	@Before
	public void checkAssumptions() {
		assumeTrue(
				"This test only makes sense if the backend supports explicit purge",
				BackendConfigurations.simple().supportsExplicitPurge()
		);
	}

	@Test
	public void simple() throws Exception {
		// tag::simple[]
		Properties jobProps = MassIndexingJob.parameters() // <1>
				.forEntities( Book.class, Author.class ) // <2>
				.build();

		JobOperator jobOperator = BatchRuntime.getJobOperator(); // <3>
		long executionId = jobOperator.start( MassIndexingJob.NAME, jobProps ); // <4>
		// end::simple[]

		JobExecution jobExecution = jobOperator.getJobExecution( executionId );
		jobExecution = waitForTermination( jobOperator, jobExecution, JOB_TIMEOUT_MS );
		assertThat( jobExecution.getBatchStatus() ).isEqualTo( BatchStatus.COMPLETED );

		with( entityManagerFactory ).runNoTransaction( entityManager -> {
			assertBookAndAuthorCount( entityManager, NUMBER_OF_BOOKS, NUMBER_OF_BOOKS );
		} );
	}

	@Test
	public void hql() throws Exception {
		// tag::hql[]
		Properties jobProps = MassIndexingJob.parameters() // <1>
				.forEntities( Author.class ) // <2>
				.restrictedBy( "from Author a where a.lastName = 'Smith1'" ) // <3>
				.build();

		JobOperator jobOperator = BatchRuntime.getJobOperator(); // <4>
		long executionId = jobOperator.start( MassIndexingJob.NAME, jobProps ); // <5>
		// end::hql[]

		JobExecution jobExecution = jobOperator.getJobExecution( executionId );
		jobExecution = waitForTermination( jobOperator, jobExecution, JOB_TIMEOUT_MS );
		assertThat( jobExecution.getBatchStatus() ).isEqualTo( BatchStatus.COMPLETED );

		with( entityManagerFactory ).runNoTransaction( entityManager -> {
			assertBookAndAuthorCount( entityManager, 0, NUMBER_OF_BOOKS / 2 );
		} );
	}

	@Override
	protected Author newAuthor(int id) {
		Author author = new Author();
		author.setId( id );
		author.setFirstName( "John" + id );
		// use the id % 2
		author.setLastName( "Smith" + ( id % 2 ) );
		return author;
	}

	void assertBookAndAuthorCount(EntityManager entityManager, int expectedBookCount, int expectedAuthorCount) {
		setupHelper.assertions().searchAfterIndexChanges(
				entityManager.getEntityManagerFactory().unwrap( SessionFactory.class ),
				() -> {
					SearchSession searchSession = Search.session( entityManager );
					assertThat( searchSession.search( Book.class )
							.where( f -> f.matchAll() )
							.fetchTotalHitCount() )
							.isEqualTo( expectedBookCount );
					assertThat( searchSession.search( Author.class )
							.where( f -> f.matchAll() )
							.fetchTotalHitCount() )
							.isEqualTo( expectedAuthorCount );
				} );
	}

	private static JobExecution waitForTermination(JobOperator jobOperator, JobExecution jobExecution, int timeoutInMs)
			throws InterruptedException {
		long endTime = System.currentTimeMillis() + timeoutInMs;

		while ( !jobExecution.getBatchStatus().equals( BatchStatus.COMPLETED )
				&& !jobExecution.getBatchStatus().equals( BatchStatus.STOPPED )
				&& !jobExecution.getBatchStatus().equals( BatchStatus.FAILED )
				&& System.currentTimeMillis() < endTime ) {

			long executionId = jobExecution.getExecutionId();
			try {
				Thread.sleep( THREAD_SLEEP );
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
			jobExecution = jobOperator.getJobExecution( executionId );
		}

		return jobExecution;
	}
}
