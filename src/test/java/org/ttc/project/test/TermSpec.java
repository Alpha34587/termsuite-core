package org.ttc.project.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.OccurrenceType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermClass;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.TermClassProvider;
import eu.project.ttc.models.index.TermClassProviders;

public class TermSpec {

	private Term term1;
	private Term term2;
	private Term term3;
	private Term term4;
	private Term term5;

	@Before
	public void setTerms() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		this.term1 = Fixtures.term1();
		this.term2 = Fixtures.term2();
		this.term3 = Fixtures.term3();
		this.term4 = Fixtures.term4();
		this.term5 = Fixtures.term5();
		
	}

	private Term termWithContext1;
	private Term termWithContext2;
	private Term termWithContext3;
	
	@Before
	public void initContexts() {
		TermIndex termIndex = Fixtures.termIndexWithOccurrences();
		termIndex.createOccurrenceIndex();
		termWithContext1 = termIndex.getTermByGroupingKey("term1");
		termWithContext2 = termIndex.getTermByGroupingKey("term2");
		termWithContext3 = termIndex.getTermByGroupingKey("term3");
		
	}
	
	private void initTermClasses() {
		TermClass termClass1 = new TermClass(termWithContext1, ImmutableSet.of(termWithContext1));
		TermClass termClass2 = new TermClass(termWithContext2, ImmutableSet.of(termWithContext2, termWithContext3));
		termWithContext1.setTermClass(termClass1);
		termWithContext2.setTermClass(termClass2);
		termWithContext3.setTermClass(termClass2);
	}
	
	@Test
	public void testGetLemmaStemKeys() {
		TermClassProvider provider = TermClassProviders.classProviders.get(TermClassProviders.KEY_WORD_COUPLE_LEMMA_STEM);
		Assert.assertEquals(
				ImmutableList.of("energie+eol"),
				provider.getClasses(term1));
		Assert.assertEquals(
				ImmutableList.of(),
				provider.getClasses(term2));
		Assert.assertEquals(
				ImmutableList.of("acces+radioelectriq", "acces+recouvr", "radioelectrique+recouvr"), 
				provider.getClasses(term3));
	}

	@Test
	public void computeContextVectorScope1() {
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 1, 1, false);
		
		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term2", 1, 0d), tuple("term3", 3, 0d));

		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 1, 0d), tuple("term3", 1, 0d));

		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 3, 0d), tuple("term2", 1, 0d));
	}

	@Test
	public void computeContextVectorScope3() {
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, false);
		
		// T1 T2 T3 T1 T3 T3 T1

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term2", 2, 0d), tuple("term3", 6, 0d));
	
		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 2, 0d), tuple("term3", 2, 0d));
	
		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(2)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 6, 0d), tuple("term2", 2, 0d));
	}


	@Test
	public void computeContextVectorWithTermClassesRaiseErrorIfNoTermClass() {
		try {
			termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
			fail("should raise error");
		} catch(IllegalStateException e) {
			// ok
		} catch(Exception e) {
			fail("Unexpected exception");
		}
		initTermClasses();
		// should not raise error
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
	}

	

		
	@Test
	public void computeContextVectorWithTermClasses() {
		initTermClasses();
		termWithContext1.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
		termWithContext2.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);
		termWithContext3.computeContextVector(OccurrenceType.SINGLE_WORD, 3, 1, true);

		assertThat(termWithContext1.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term2", 8, 0d));
	
		assertThat(termWithContext2.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 2, 0d));
	
		assertThat(termWithContext3.getContextVector().getEntries())
			.hasSize(1)
			.extracting("coTerm.groupingKey", "nbCooccs", "assocRate")
			.contains(tuple("term1", 6, 0d));
	}

	@Test
	public void testAddSyntacticVariant() {
		assertThat(this.term5.getSyntacticVariants()).hasSize(0);
		assertThat(this.term5.getSyntacticBases()).hasSize(0);
		assertThat(this.term3.getSyntacticVariants()).hasSize(0);
		assertThat(this.term3.getSyntacticBases()).hasSize(0);
		assertThat(this.term4.getSyntacticVariants()).hasSize(0);
		assertThat(this.term4.getSyntacticBases()).hasSize(0);
		
		term5.addSyntacticVariant(term3, "Tata");
		assertThat(this.term5.getSyntacticVariants()).hasSize(1);
		assertThat(this.term5.getSyntacticBases()).hasSize(0);
		assertThat(this.term3.getSyntacticVariants()).hasSize(0);
		assertThat(this.term3.getSyntacticBases()).hasSize(1);
		assertThat(this.term3.getSyntacticBases()).extracting("variationRule").containsExactly("Tata");
		
		term5.addSyntacticVariant(term4, "Tata");
		assertThat(this.term5.getSyntacticVariants()).hasSize(2);
		assertThat(this.term5.getSyntacticBases()).hasSize(0);
		assertThat(this.term3.getSyntacticVariants()).hasSize(0);
		assertThat(this.term3.getSyntacticBases()).hasSize(1);
		assertThat(this.term4.getSyntacticVariants()).hasSize(0);
		assertThat(this.term4.getSyntacticBases()).hasSize(1);
		assertThat(this.term5.getSyntacticVariants()).extracting("variationRule").containsExactly("Tata","Tata");
		
		term5.addSyntacticVariant(term3, "Tata");
		assertThat(this.term5.getSyntacticVariants()).hasSize(2);
		assertThat(this.term5.getSyntacticBases()).hasSize(0);
		assertThat(this.term3.getSyntacticVariants()).hasSize(0);
		assertThat(this.term3.getSyntacticBases()).hasSize(1);
		assertThat(this.term4.getSyntacticVariants()).hasSize(0);
		assertThat(this.term4.getSyntacticBases()).hasSize(1);
		assertThat(this.term5.getSyntacticVariants()).extracting("variationRule").containsExactly("Tata","Tata");
		
	}
	
	@Test
	public void testGetVariants() {
		Comparator<Term> comp = TermProperty.FREQUENCY.getComparator(true);
		assertThat(term5.getVariants(0, comp)).isEmpty();

		term5.addSyntacticVariant(term3, "Tata");
		assertThat(term5.getVariants(0, comp)).isEmpty();
		assertThat(term5.getVariants(1, comp)).containsExactly(term3);
		assertThat(term5.getVariants(10, comp)).containsExactly(term3);
		
		term3.addSyntacticVariant(term4, "Toto");
		assertThat(term5.getVariants(0, comp)).isEmpty();
		assertThat(term5.getVariants(1, comp)).hasSize(1).contains(term3);
		assertThat(term5.getVariants(2, comp)).hasSize(2).contains(term3, term4);
		assertThat(term5.getVariants(10, comp)).hasSize(2).contains(term3, term4);
		
		// handles cycles
		term5.addSyntacticVariant(term5, "Toto");
		assertThat(term5.getVariants(0, comp)).isEmpty();
		assertThat(term5.getVariants(1, comp)).hasSize(1).contains(term3);
		assertThat(term5.getVariants(2, comp)).hasSize(2).contains(term3, term4);
		assertThat(term5.getVariants(3, comp)).hasSize(2).contains(term3, term4);
		assertThat(term5.getVariants(10, comp)).hasSize(2).contains(term3, term4);
		
	}
	
	@Test
	public void testGetLemmaKeys() {
		TermClassProvider provider = TermClassProviders.classProviders.get(TermClassProviders.KEY_WORD_LEMMA);
		Assert.assertEquals(
				ImmutableList.of("énergie", "éolien"),
				provider.getClasses(term1));
		Assert.assertEquals(
				ImmutableList.of("radio","électrique"),
				provider.getClasses(term2));
		Assert.assertEquals(
				ImmutableList.of("accès", "radio", "électrique", "de", "recouvrement"), 
				provider.getClasses(term3));
	}
}