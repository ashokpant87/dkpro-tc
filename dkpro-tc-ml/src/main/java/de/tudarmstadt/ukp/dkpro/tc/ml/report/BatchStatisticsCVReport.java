/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.ml.report;

import java.io.StringReader;
import java.io.StringWriter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.lab.reporting.BatchReportBase;
import de.tudarmstadt.ukp.dkpro.lab.storage.impl.StringAdapter;
import de.tudarmstadt.ukp.dkpro.lab.task.TaskContextMetadata;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;


/**
 * 
 * Collects statistical evaluation results from CV BatchTasks. Needs to be run on top level of CV setups.
 *
 * @author Johannes Daxenberger
 *
 */
public class BatchStatisticsCVReport
    extends BatchReportBase
    implements Constants
{

    @Override
    public void execute()
        throws Exception
    {
        StringWriter sWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(sWriter, ';');
        
        for (TaskContextMetadata subcontext : getSubtasks()) {
        	// FIXME this is a really bad hack
            if (subcontext.getType().contains("$1")) {
            	String csvText = getContext().getStorageService().retrieveBinary(subcontext.getId(), 
            			CV_R_CONNECT_REPORT_FILE, new StringAdapter()).getString();
            	CSVReader csvReader = new CSVReader(new StringReader(csvText),';');
            	for (String[] line : csvReader.readAll()) {
            		csvWriter.writeNext(line);
				}
                csvReader.close();
            }
        }
        getContext().storeBinary(CV_R_CONNECT_REPORT_FILE, new StringAdapter(sWriter.toString()));
        csvWriter.close();
    }
}