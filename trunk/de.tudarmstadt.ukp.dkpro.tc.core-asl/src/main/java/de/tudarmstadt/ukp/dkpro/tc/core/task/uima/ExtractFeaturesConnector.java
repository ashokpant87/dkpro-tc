package de.tudarmstadt.ukp.dkpro.tc.core.task.uima;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Instance;
import de.tudarmstadt.ukp.dkpro.tc.api.features.PairFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.core.feature.AddIdFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.core.io.DataWriter;
import de.tudarmstadt.ukp.dkpro.tc.core.task.ExtractFeaturesTask;
import de.tudarmstadt.ukp.dkpro.tc.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.fstore.simple.SimpleFeatureStore;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationFocus;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.tc.type.TextClassificationUnit;

/**
 * UIMA analysis engine that is used in the {@link ExtractFeaturesTask} to apply the feature
 * extractors on each CAS.
 * 
 * @author zesch
 * 
 */
public class ExtractFeaturesConnector
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
    @ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
    private File outputDirectory;

    public static final String PARAM_FEATURE_EXTRACTORS = "featureExtractors";
    @ExternalResource(key = PARAM_FEATURE_EXTRACTORS, mandatory = true)
    protected FeatureExtractorResource_ImplBase[] featureExtractors;

    public static final String PARAM_DATA_WRITER_CLASS = "dataWriterClass";
    @ConfigurationParameter(name = PARAM_DATA_WRITER_CLASS, mandatory = true)
    private String dataWriterClass;

    public static final String PARAM_LEARNING_MODE = "learningMode";
    @ConfigurationParameter(name = PARAM_LEARNING_MODE, mandatory = true, defaultValue = Constants.LM_SINGLE_LABEL)
    private String learningMode;

    public static final String PARAM_FEATURE_MODE = "featureMode";
    @ConfigurationParameter(name = PARAM_FEATURE_MODE, mandatory = true, defaultValue = Constants.FM_DOCUMENT)
    private String featureMode;

    public static final String PARAM_ADD_INSTANCE_ID = "addInstanceId";
    @ConfigurationParameter(name = PARAM_ADD_INSTANCE_ID, mandatory = true, defaultValue = "true")
    private boolean addInstanceId;

    protected FeatureStore featureStore;
    
    private int sequenceId;
    
    private JCas jcas;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        featureStore = new SimpleFeatureStore();
        
        sequenceId = 1;

        if (featureExtractors.length == 0) {
            context.getLogger().log(Level.SEVERE, "No feature extractors have been defined.");
            throw new ResourceInitializationException();
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        this.jcas = jcas;
        
        List<Instance> instances = new ArrayList<Instance>();
        if (learningMode.equals(Constants.LM_SEQUENCE)) {
            instances = getMultipleInstances();
            sequenceId++;
        }
        else {
            instances.add(getSingleInstance());
        }
        
        for (Instance instance : instances) {
            this.featureStore.addInstance(instance);
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        // addInstanceId requires dense instances
        try {
            DataWriter writer = (DataWriter) Class.forName(dataWriterClass).newInstance();
            writer.write(outputDirectory, featureStore, true, learningMode);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    private Instance getSingleInstance()
            throws AnalysisEngineProcessException
    {
        Instance instance = new Instance();
        
        if (featureMode.equals(Constants.FM_DOCUMENT))
        {
            try {
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof DocumentFeatureExtractor)) {
                        throw new TextClassificationException("Using non-document FE in document mode: " + featExt.getResourceName());
                    }
                    
                    instance.addFeatures(((DocumentFeatureExtractor) featExt).extract(jcas));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }           
        }
        else if (featureMode.equals(Constants.FM_PAIR))
        {
            try {
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof PairFeatureExtractor)) {
                        throw new TextClassificationException("Using non-pair FE in pair mode: " + featExt.getResourceName());
                    }
                    JCas view1 = jcas.getView(Constants.PART_ONE);
                    JCas view2 = jcas.getView(Constants.PART_TWO);
                    instance.addFeatures(((PairFeatureExtractor) featExt).extract(view1, view2));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            catch (CASException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        else if (featureMode.equals(Constants.FM_UNIT))
        {
            try {
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof ClassificationUnitFeatureExtractor)) {
                        throw new TextClassificationException("Using non-unit FE in unit mode: " + featExt.getResourceName());
                    }
                    TextClassificationFocus focus = JCasUtil.selectSingle(jcas,
                            TextClassificationFocus.class);
                    Collection<TextClassificationUnit> classificationUnits = JCasUtil
                            .selectCovered(jcas, TextClassificationUnit.class, focus);

                    if (classificationUnits.size() != 1) {
                        throw new AnalysisEngineProcessException(
                                "There are more than one TextClassificationUnit anotations in the JCas.",
                                null);
                    }

                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, classificationUnits.iterator().next()));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        
        if (addInstanceId) {
            // TODO does this FE need to be created again every time?
            DocumentFeatureExtractor extractor = new AddIdFeatureExtractor();
            try {
                instance.addFeatures(extractor.extract(jcas));
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        // set and write outcome label(s)
        instance.setOutcomes(getOutcomes(null));
        
        return instance;
    }
    
    
    private List<Instance> getMultipleInstances()
            throws AnalysisEngineProcessException
    {
        List<Instance> instances = new ArrayList<Instance>();
        
        TextClassificationFocus focus = JCasUtil.selectSingle(jcas, TextClassificationFocus.class);
        Collection<TextClassificationUnit> units = JCasUtil.selectCovered(jcas, TextClassificationUnit.class, focus);
        
        List<Feature> instanceId;
        try {
            // TODO does this FE need to be created again every time?
            DocumentFeatureExtractor addIdExtractor = new AddIdFeatureExtractor();
            instanceId = addIdExtractor.extract(jcas);
        }
        catch (TextClassificationException e) {
            throw new AnalysisEngineProcessException(e);
        }
        for (TextClassificationUnit unit : units) {
            Instance instance = new Instance();
          
            if (addInstanceId) {
                instance.addFeatures(instanceId);
            }
            
            try {
                if (!featureMode.equals(Constants.FM_UNIT))
                {
                    throw new TextClassificationException("Feature mode should be unit when sequence labelling learning mode is selected.");
                }
                
                for (FeatureExtractorResource_ImplBase featExt : featureExtractors) {
                    if (!(featExt instanceof ClassificationUnitFeatureExtractor)) {
                        throw new TextClassificationException("Using non-unit FE in unit mode: " + featExt.getResourceName());
                    }                

                    instance.addFeatures(((ClassificationUnitFeatureExtractor) featExt).extract(
                            jcas, unit));
                }
            }
            catch (TextClassificationException e) {
                throw new AnalysisEngineProcessException(e);
            }
            
            // set and write outcome label(s)
            instance.setOutcomes(getOutcomes(unit));
            instance.setSequenceId(sequenceId);
            
            instances.add(instance);
        }
        
        return instances;      
    }
    
    private List<String> getOutcomes(AnnotationFS unit) 
            throws AnalysisEngineProcessException
    {
        Collection<TextClassificationOutcome> outcomes;
        if (unit == null) {
            outcomes = JCasUtil.select(jcas, TextClassificationOutcome.class);
        }
        else {
            outcomes = JCasUtil.selectCovered(jcas, TextClassificationOutcome.class, unit);
        }      

        if (outcomes.size() == 0) {
            throw new AnalysisEngineProcessException(new TextClassificationException(
                    "No outcome annotations present in current CAS."));
        }

        List<String> stringOutcomes = new ArrayList<String>();
        for (TextClassificationOutcome outcome : outcomes) {
            stringOutcomes.add(outcome.getOutcome());
        }
        
        return stringOutcomes;
    }
}