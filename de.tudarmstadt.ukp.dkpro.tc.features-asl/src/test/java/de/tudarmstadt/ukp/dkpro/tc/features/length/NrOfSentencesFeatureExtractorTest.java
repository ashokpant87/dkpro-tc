package de.tudarmstadt.ukp.dkpro.tc.features.length;

import static de.tudarmstadt.ukp.dkpro.tc.features.util.FeatureTestUtil.assertFeature;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;

public class NrOfSentencesFeatureExtractorTest
{
    @Test
    public void nrOfSentencesFeatureExtractorTest()
        throws Exception
    {
        AnalysisEngineDescription desc = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class)
        );
        AnalysisEngine engine = createPrimitive(desc);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test! Does it thes sentences? Oh yes, it does!");
        engine.process(jcas);
        
        NrOfSentencesFeatureExtractor extractor = new NrOfSentencesFeatureExtractor();
        List<Feature> features = extractor.extract(jcas, null);

        Assert.assertEquals(1, features.size());
        
        Iterator<Feature> iter = features.iterator();
        assertFeature(NrOfSentencesFeatureExtractor.FN_NR_OF_SENTENCES, 3, iter.next());
    }
}