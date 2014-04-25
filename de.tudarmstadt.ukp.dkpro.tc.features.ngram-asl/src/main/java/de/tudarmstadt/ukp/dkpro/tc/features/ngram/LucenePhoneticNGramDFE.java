package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.base.LucenePhoneticNGramFeatureExtractorBase;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.util.NGramUtils;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class LucenePhoneticNGramDFE
    extends LucenePhoneticNGramFeatureExtractorBase
    implements DocumentFeatureExtractor
{

    @Override
    public List<Feature> extract(JCas jcas)
        throws TextClassificationException
    {
    	
        List<Feature> features = new ArrayList<Feature>();
        FrequencyDistribution<String> documentNgrams = NGramUtils.getDocumentPhoneticNgrams(jcas, phoneticNgramMinN, phoneticNgramMaxN);

        for (String topNgram : topKSet.getKeys()) {
            if (documentNgrams.getKeys().contains(topNgram)) {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 1));
            }
            else {
                features.add(new Feature(getFeaturePrefix() + "_" + topNgram, 0));
            }
        }
        return features;
    }
}