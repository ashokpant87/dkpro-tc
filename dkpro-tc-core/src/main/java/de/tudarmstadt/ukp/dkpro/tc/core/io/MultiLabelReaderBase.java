package de.tudarmstadt.ukp.dkpro.tc.core.io;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderMultiLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

/**
 * Base class for multi label readers.
 *
 */
public abstract class MultiLabelReaderBase
    extends TextReader
    implements TCReaderMultiLabel
{

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        super.getNext(aCAS);
        
        JCas jcas;
        try {
            jcas = aCAS.getJCas();
        }
        catch (CASException e) {
            throw new CollectionException();
        }

        for (String outcomeValue : getTextClassificationOutcomes(jcas)) {
            TextClassificationOutcome outcome = new TextClassificationOutcome(jcas);
            outcome.setOutcome(outcomeValue);
            outcome.addToIndexes();
        }
    }
}