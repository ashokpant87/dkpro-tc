package de.tudarmstadt.ukp.dkpro.tc.features.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramMetaCollector;
import de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta.NGramUtils;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class NGramFeatureExtractor
    extends NGramFeatureExtractorBase
{
    public static final String PARAM_NGRAM_FD_FILE = "ngramFdFile";
    @ConfigurationParameter(name = PARAM_NGRAM_FD_FILE, mandatory = true)
    private String ngramFdFile;

    // FIXME as this is no parameter, the other branch cannot be accessed
    private boolean useFreqThreshold = false;
    private FrequencyDistribution<String> trainingFD;

    @Override
    public List<Class<? extends MetaCollector>> getMetaCollectorClasses()
    {
        List<Class<? extends MetaCollector>> metaCollectorClasses = new ArrayList<Class<? extends MetaCollector>>();
        metaCollectorClasses.add(NGramMetaCollector.class);

        return metaCollectorClasses;
    }

    @Override
    protected FrequencyDistribution<String> getTopNgrams()
        throws ResourceInitializationException
    {
        try {
            trainingFD = new FrequencyDistribution<String>();
            trainingFD.load(new File(ngramFdFile));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        FrequencyDistribution<String> topNGrams = new FrequencyDistribution<String>();

        if (useFreqThreshold) {
            double total = trainingFD.getN();
            double max = 0;
            for (String key : trainingFD.getKeys()) {
                double freq = trainingFD.getCount(key) / total;
                max = Math.max(max, freq);
                if (freq >= ngramFreqThreshold) {
                    topNGrams.addSample(key, trainingFD.getCount(key));
                }
            }
        }
        else {

            // FIXME - this is a really bad hack, but currently no better FD method to return
            // topK samples each of size n or greater.

            Map<String, Long> map = new HashMap<String, Long>();

            for (String key : trainingFD.getKeys()) {
                map.put(key, trainingFD.getCount(key));
            }

            Map<String, Long> sorted_map = new TreeMap<String, Long>(new ValueComparator(map));
            sorted_map.putAll(map);

            int i = 0;
            for (String key : sorted_map.keySet()) {
                if (i >= ngramUseTopK) {
                    break;
                }
                topNGrams.addSample(key, trainingFD.getCount(key));
                i++;
            }
        }

        getLogger().log(Level.INFO, "+++ TAKING " + topNGrams.getKeys().size() + " NGRAMS");

        return topNGrams;
    }

    class ValueComparator
        implements Comparator<String>
    {

        Map<String, Long> base;

        public ValueComparator(Map<String, Long> base)
        {
            this.base = base;
        }

        @Override
        public int compare(String a, String b)
        {

            if (base.get(a) < base.get(b)) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }

    @Override
    protected String getFeaturePrefix()
    {
        return "ngram";
    }

    // FIXME this is duplicated in LuceneNGramFeatureExtractor currently
    // I did not resolve this, as we might get rid of this version anyway
    // if we keep it, maybe there should be a TokenNGramFeatureExtractor base class that both
    // inherit from
    @Override
    protected FrequencyDistribution<String> getDocumentNgrams(JCas jcas)
    {
        return NGramUtils.getDocumentNgrams(jcas, ngramLowerCase, filterPartialStopwordMatches,
                ngramMinN, ngramMaxN, stopwords);
    }

    @Override
    protected FrequencyDistribution<String> getAnnotationNgrams(JCas jcas, Annotation anno)
    {
        return NGramUtils.getAnnotationNgrams(jcas, anno, ngramLowerCase,
                filterPartialStopwordMatches, ngramMinN, ngramMaxN, stopwords);
    }
}