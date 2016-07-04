package eu.project.ttc.tools.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Component;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.Word;
import eu.project.ttc.utils.TermIndexUtils;

/**
 * 
 * A tool that generates all control files required for 
 * functional tests from a {@link TermIndex}.
 * 
 * @author Damien Cram
 *
 */
public class ControlFilesGenerator {
	
	private TermIndex termIndex;
	
	
	public ControlFilesGenerator(TermIndex termIndex) {
		super();
		this.termIndex = termIndex;
	}


	/**
	 * 
	 * @param directory
	 * 			the directory where to create the files.
	 */
	public void generate(File directory) throws IOException {
		if(!directory.exists())
			directory.mkdirs();
		
		Set<String> distinctRuleNames = Sets.newHashSet();
		Set<TermVariation> variations = TermIndexUtils.getVariations(termIndex);
		for(TermVariation tv:variations)
			if(tv.getVariationType() == VariationType.SYNTACTICAL || tv.getVariationType() == VariationType.MORPHOLOGICAL)
				distinctRuleNames.add((String)tv.getInfo());

		/*
		 * Write syntactic rules
		 */
		for(String ruleName:distinctRuleNames) {
			String pathname = directory.getAbsolutePath() + "/" + getSyntacticRuleFileName(ruleName);
			writeVariations(pathname, TermIndexUtils.selectTermVariations(termIndex, VariationType.SYNTACTICAL, ruleName));
		}
		
		/*
		 * Write prefix variations
		 */
		String prefixPath = directory.getAbsolutePath() + "/" + getPrefixFileName();
		writeVariations(prefixPath, TermIndexUtils.selectTermVariations(termIndex, VariationType.IS_PREFIX_OF));
		

		/*
		 * Write derivative variations
		 */
		String derivativePath = directory.getAbsolutePath() + "/" + getDerivatesFileName();
		writeVariations(derivativePath, TermIndexUtils.selectTermVariations(termIndex, VariationType.DERIVES_INTO));

		/*
		 * Write compounds
		 */
		String compoundPath = directory.getAbsolutePath() + "/" + getCompoundsFileName();
		writeCompounds(compoundPath);
		
	}

	public static String getCompoundsFileName() {
		return "compounds.tsv";
	}

	public static String getDerivatesFileName() {
		return "derivates.tsv";
	}

	public static String getPrefixFileName() {
		return "prefixes.tsv";
	}

	public static String getSyntacticRuleFileName(String ruleName) {
		return "syntactic-" + ruleNametoFileName(ruleName) + ".tsv";
	}


	public static String ruleNametoFileName(String ruleName) {
		return ruleName.replaceAll("\\|", "-or-");
	}

	private void writeCompounds(String filePath) throws IOException {
		Writer writer = new FileWriter(filePath);
		for(Term t:termIndex.getTerms()) {
			if(t.isSingleWord() && t.isCompound()) {
				writer.append(String.format("%s\t%s\t%s%n", 
						t.getGroupingKey(),
						t.getWords().get(0).getWord().getCompoundType(),
						toCompoundString(t)
					)
				);
			}
		}
		writer.flush();
		writer.close();
		
	}


	public static String toCompoundString(Term t) {
		Preconditions.checkArgument(t.isSingleWord(), "Term %s should be a single-word term", t);
		Preconditions.checkArgument(t.isCompound(), "Term %s should be compound", t);
		List<String> componentStrings = Lists.newArrayList();
		Word word = t.getWords().get(0).getWord();
		for(Component c:word.getComponents()) {
			componentStrings.add(String.format("%s:%s", 
					word.getLemma().substring(c.getBegin(), c.getEnd()),
					c.getLemma()
					));
		}
		
		return Joiner.on("|").join(componentStrings);
	}
	
	private void writeVariations(String path, Collection<TermVariation> variations) throws IOException {
		Writer writer = new FileWriter(path);
		for(TermVariation tv:variations) {
			writer.append(String.format("%s\t%s\t%s\t%s%n", 
					tv.getBase().getGroupingKey(),
					tv.getVariant().getGroupingKey(),
					tv.getVariationType(),
					tv.getInfo()
				));
		}
		writer.flush();
		writer.close();
	}

	


}
