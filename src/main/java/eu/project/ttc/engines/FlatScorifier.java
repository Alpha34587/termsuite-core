package eu.project.ttc.engines;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.resources.ScoredModel;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.utils.StringUtils;
import fr.univnantes.lina.UIMAProfiler;

public class FlatScorifier extends JCasAnnotator_ImplBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlatScorifier.class);

	@ExternalResource(key=ScoredModel.SCORED_MODEL, mandatory=true)
	private ScoredModel scoredModel;

	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	public static final String EXTENSION_SPEC_TH = "ExtensionSpecTh";
	@ConfigurationParameter(name=EXTENSION_SPEC_TH, mandatory=false, defaultValue="0")
	private double extensionSpecTh;

	public static final String EXTENSION_GAIN_TH = "ExtensionGainTh";
	@ConfigurationParameter(name=EXTENSION_GAIN_TH, mandatory=false, defaultValue="0")
	private double extensionGainTh;

	public static final String VARIANT_INDEPENDANCE_TH = "VariantIndependanceTh";
	@ConfigurationParameter(name=VARIANT_INDEPENDANCE_TH, mandatory=false, defaultValue="0")
	private double variantIndependanceTh;

	public static final String VARIATION_TH = "VariationTh";
	@ConfigurationParameter(name=VARIATION_TH, mandatory=false, defaultValue="0")
	private double variationScoreTh;

	public static final String ORTHOGRAPHIC_SCORE_TH = "OrthographicScoreTh";
	@ConfigurationParameter(name=ORTHOGRAPHIC_SCORE_TH, mandatory=false, defaultValue="0.55")
	private double orthographicScoreTh;
	
	public static final String TERM_INDEPENDANCE_TH = "TermIndependanceTh";
	@ConfigurationParameter(name=TERM_INDEPENDANCE_TH, mandatory=false, defaultValue="0.10")
	private double termIndependanceTh;

	
	private static Comparator<ScoredTerm> wrComparator = new Comparator<ScoredTerm>() {
		@Override
		public int compare(ScoredTerm o1, ScoredTerm o2) {
			return Double.compare(o2.getWRLog(), o1.getWRLog());
		}
	};

	private static Comparator<ScoredVariation> variationScoreComparator = new Comparator<ScoredVariation>() {
		@Override
		public int compare(ScoredVariation o1, ScoredVariation o2) {
			return ComparisonChain.start()
					.compare(o2.getVariationScore(), o1.getVariationScore())
					.compare(o1.getTerm().getGroupingKey(), o2.getTerm().getGroupingKey())
					.compare(o1.getVariant().getTerm().getGroupingKey(), o2.getVariant().getTerm().getGroupingKey())
					.result();
			
		}
	};
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// do nothing
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		UIMAProfiler.getProfiler("AnalysisEngine").start(this, "process");
		LOGGER.info("Start flat scorifier");

		// Filter terms with bad orthgraph
		doScoredModel();

		UIMAProfiler.getProfiler("AnalysisEngine").stop(this, "process");
	}

	private void doScoredModel() {
		this.scoredModel.importTermIndex(termIndexResource.getTermIndex());
		this.scoredModel.sort(wrComparator);
		
		int size = this.scoredModel.getTerms().size();
		filterTerms();
		LOGGER.info("Filtered {} terms out of {}", size - this.scoredModel.getTerms().size(), size);
		
		int sizeBefore = 0;
		int sizeAfter = 0;
		for(ScoredTerm t:this.scoredModel.getTerms()) {
			if(t.getVariations().isEmpty())
				continue;
			List<ScoredVariation> sv = Lists.newArrayListWithExpectedSize(t.getVariations().size());
			sv.addAll(t.getVariations());
			sizeBefore += sv.size();
			filterVariations(sv);
			sizeAfter += sv.size();
			Collections.sort(sv, variationScoreComparator);
			t.setVariations(sv);
		}
		
		LOGGER.info("Filtered {} variants out of {}", sizeBefore - sizeAfter, sizeBefore);
		this.scoredModel.sort(wrComparator);
	}

	private void filterTerms() {
		Set<ScoredTerm> rem = Sets.newHashSet();
		for(ScoredTerm st:scoredModel.getTerms()) {
			if(StringUtils.getOrthographicScore(st.getTerm().getLemma()) < this.orthographicScoreTh)
				rem.add(st);
			else if(st.getTermIndependanceScore() < this.termIndependanceTh)
				rem.add(st);
		}
		this.scoredModel.removeTerms(rem);
	}

	private void filterVariations(List<ScoredVariation>  inputTerms) {
		Iterator<ScoredVariation> it = inputTerms.iterator();
		ScoredVariation v;
		while(it.hasNext()) {
			v = it.next();
			if(v.getVariantIndependanceScore() < variantIndependanceTh
					|| v.getVariationScore() < variationScoreTh
					) {
				it.remove();
			}
			else if(v.getExtensionAffix() != null) {
				if(v.getExtensionGainScore() < extensionGainTh
						|| v.getExtensionSpecScore() < extensionSpecTh)
					it.remove();
			}
		}
		
	}
}
