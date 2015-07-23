package eu.project.ttc.utils;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.project.ttc.engines.cleaner.TermProperty;
import eu.project.ttc.models.ContextVector;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.resources.BilingualDictionary;

public class AlignerUtils {
	public static final int TRANSLATION_STRATEGY_PRORATA = 1;
	public static final int TRANSLATION_STRATEGY_MOST_FREQUENT = 2;
	public static final int TRANSLATION_STRATEGY_MOST_SPECIFIC = 3;
	private static final int TRANSLATION_STRATEGY_EQUI_REPARTITION = 4;
	
	
	/**
	 *
	 * Translates all {@link ContextVector} components (i.e. its coTerms) into
	 * the target language of this aligner by the mean of one of the available 
	 * strategy :
	 *  - {@link AlignerUtils#TRANSLATION_STRATEGY_MOST_FREQUENT}
	 *  - {@link AlignerUtils#TRANSLATION_STRATEGY_PRORATA}
	 *  - {@link AlignerUtils#TRANSLATION_STRATEGY_EQUI_REPARTITION} 
	 *  - {@link AlignerUtils#TRANSLATION_STRATEGY_MOST_SPECIFIC} 
	 *
	 * @see BilingualDictionary
	 * @param sourceVector
	 * 			The source context vector object to be translated into target language
	 * @param dictionary
	 * 			The dico used in the translation process
	 * @param translationStrategy
	 * 			The translation strategy of the <code>sourceVector</code>. 
	 * 			Two possible values: {@link AlignerUtils#TRANSLATION_STRATEGY_MOST_FREQUENT}
	 * 							     {@link AlignerUtils#TRANSLATION_STRATEGY_PRORATA} 
	 * 							     {@link AlignerUtils#TRANSLATION_STRATEGY_EQUI_REPARTITION} 
	 * 							     {@link AlignerUtils#TRANSLATION_STRATEGY_MOST_SPECIFIC} 
	 * @return
	 * 			The translated context vector
	 */
	public static ContextVector translateVector(ContextVector sourceVector, 
			BilingualDictionary dictionary, int translationStrategy, TermIndex targetTermino) {
		ContextVector targetVector = new ContextVector();
		for(ContextVector.Entry entry:sourceVector.getEntries()) {
			Set<Term> translations = Sets.newHashSet();
			for(String targetLemma:dictionary.getTranslations(entry.getCoTerm().getLemma())) {
				Collection<Term> translatedTerms = targetTermino.getSingleWordTermByLemma(targetLemma);
				if(!translatedTerms.isEmpty()) 
					translations.add(translatedTerms.iterator().next());
			}
			switch (translationStrategy) {
			case TRANSLATION_STRATEGY_PRORATA:
				fillTargetVectorSProrata(targetVector, entry, translations);
				break;
			case TRANSLATION_STRATEGY_MOST_FREQUENT:
				fillTargetVectorSMostFrequent(targetVector, entry, translations);
				break;
			case TRANSLATION_STRATEGY_MOST_SPECIFIC:
				fillTargetVectorSMostSpecificity(targetVector, entry, translations);
				break;
			case TRANSLATION_STRATEGY_EQUI_REPARTITION:
				fillTargetVectorSEquiRepartition(targetVector, entry, translations);
				break;
			default:
				throw new IllegalArgumentException("Invalid translation strategy: " + translationStrategy);
			}
		}
		return targetVector;
	}
	

	/**
	 * This method implements the strategy {@link #TRANSLATION_STRATEGY_PRORATA} 
	 * for context vector translation.
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 * Suppose that frequencies in target term index are : 
	 *   - black : 35
	 *   - dark : 15
	 *   - dog : 7
	 *   
	 * The translated vector would be : <black: 7, dark: 3, dog: 3>
	 * 
	 * because :
	 *   - total frequency in target term index for term "noir" is 35 + 15 = 50,
	 *     and 7 = ( 35 / 50 ) * 10 for "black"
	 *     and 3 = ( 15 / 50 ) * 10 for "dark"
	 *   - total frequency in target term index for term "dog" is 7,
	 *     and 3 = ( 7 / 7 ) * 3
	 *     
	 * 
	 * @param translatedVector
	 * 			the target vector to be fill 
	 * @param sourceTermEntry
	 * 			the source vector's component to translated and add to target vector
	 * @param candidateTranslations
	 * 			the candidate translations of the <code>sourceTermEntry</code> given by the
	 * 			bilingual dictionary.
	 */
	private static void fillTargetVectorSProrata(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		/*
		 * Do the cross product of translation frequencies
		 */
		int totalFreqInTargetTermino = 0;
		for(Term tt : candidateTranslations) 
			totalFreqInTargetTermino += tt.getFrequency();
		
		for(Term targetTerm:candidateTranslations) {
			int prorataCooccs = targetTerm.getFrequency() * sourceTermEntry.getNbCooccs() / totalFreqInTargetTermino;
			translatedVector.addEntry(targetTerm, prorataCooccs, sourceTermEntry.getAssocRate());
		}
	}
	
	/**
	 * This method implements the {@value #TRANSLATION_STRATEGY_MOST_FREQUENT} 
	 * strategy for context vector translation.
	 * 
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 * Suppose that frequencies in target term index are : 
	 *   - black : 35
	 *   - dark : 15
	 *   - dog : 7
	 *   
	 * The translated vector would be : <black: 10, dog: 3>
	 * 
	 * @param translatedVector
	 * 			the target vector to be fill 
	 * @param sourceTermEntry
	 * 			the source vector's component to translated and add to target vector
	 * @param candidateTranslations
	 * 			the candidate translations of the <code>sourceTermEntry</code> given by the
	 * 			bilingual dictionary.
	 * 
	 */
	private static void fillTargetVectorSMostFrequent(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		fillTargetVectorWithMostProperty(translatedVector, sourceTermEntry,
				candidateTranslations, TermProperty.FREQUENCY);
	}
	
	/**
	 * 
	 * Same as {@link #fillTargetVectorSMostFrequent(ContextVector, eu.project.ttc.models.ContextVector.Entry, Set)}
	 * but applies on specificity property.
	 * 
	 * @see #fillTargetVectorSMostFrequent(ContextVector, eu.project.ttc.models.ContextVector.Entry, Set)
	 * @param translatedVector
	 * @param sourceTermEntry
	 * @param candidateTranslations
	 */
	private static void fillTargetVectorSMostSpecificity(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		fillTargetVectorWithMostProperty(translatedVector, sourceTermEntry,
				candidateTranslations, TermProperty.WR);
	}
	
	/**
	 * 
	 * Explanation of strategy:
	 * 
	 * Example of source term in french : chat <noir: 10, chien: 3>
	 * 
	 * Example of candidate translations for "noir" from dico: black, dark
	 * Example of candidate translations for "chien" from dico: dog
	 * 
	 *   
	 * The translated vector would be : <black: 5,  dark: 5, dog: 3>
	 * 
	 * @param translatedVector
	 * @param sourceTermEntry
	 * @param candidateTranslations
	 */
	private static  void fillTargetVectorSEquiRepartition(ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry, Set<Term> candidateTranslations) {
		/*
		 * Do the cross product of translation frequencies
		 */
		for(Term targetTerm:candidateTranslations) {
			int nbCooccs = sourceTermEntry.getNbCooccs()/candidateTranslations.size();
			translatedVector.addEntry(
					targetTerm, 
					nbCooccs, 
					sourceTermEntry.getAssocRate()/candidateTranslations.size());
		}
	}

	private static void fillTargetVectorWithMostProperty(
			ContextVector translatedVector,
			ContextVector.Entry sourceTermEntry,
			Set<Term> candidateTranslations, final TermProperty frequency) {
		Term mostFrequent = null;
		double maxValue = -1d;
		
		for(Term t:candidateTranslations) {
			if(frequency.getDoubleValue(t)>maxValue) {
				maxValue = t.getFrequency();
				mostFrequent = t;
			}
		}
		
		if(mostFrequent != null) 
			/*
			 * mostFrequent would be null if candidateTranslations is empty
			 */
			translatedVector.addEntry(mostFrequent, sourceTermEntry.getNbCooccs(), sourceTermEntry.getAssocRate());
	}


}