/*
 * LIMES Core Library - LIMES – Link Discovery Framework for Metric Spaces.
 * Copyright © 2011 Data Science Group (DICE) (ngonga@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 */
package org.aksw.limes.core.io.serializer;

import org.aksw.limes.core.datastrutures.EvaluationRun;
import org.aksw.limes.core.evaluation.evaluator.EvaluatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * This class serializes the results of evaluating a machine learning algorithm that uses a dataset against one or more of evaluation measures<br>
 * @author Mofeed Hassan (mounir@informatik.uni-leipzig.de)
 * @version 1.0
 * @since 1.0
 *
 */
public class EvaluationSerlializer {
    private static Logger logger = LoggerFactory.getLogger(EvaluationSerlializer.class.getName());
    /* The file extension of the serialized data */
    private String extension="txt";
    /* The separator of the serialized data */
    private String separator=",";
    /* The writer to write the results into a file*/
    protected PrintWriter writer;
    /* The path to write the results into a file*/
    protected File folder = new File("");
    private final int stringSize=200;

    public EvaluationSerlializer(){}

    private boolean open(String file) {
        try {
            // if no parent folder is given, then take that of the config that was set by the controller
            if (!file.contains("/") && !file.contains("\\")) {
                String filePath = folder.getAbsolutePath() + File.separatorChar + file;
                writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
            } else {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            }
        } catch (Exception e) {
            logger.warn("Error creating PrintWriter");
            logger.warn(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean close() {
        try {
            writer.close();
        } catch (Exception e) {
            logger.warn("Error closing PrintWriter");
            logger.warn(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Gets a list of evaluation run's results and serializes it to a file using the specified separator. The method
     * assume that the class already knows the separator which specifies the file extension too
     *
     * @param evaluation List to serialize
     * @param file   The file in which the mapping is to be serialized
     */

    public void writeToFile(List<EvaluationRun> evaluation, String file) {

        open(file);
        if (evaluation.size() > 0) {
            String headers =createHeader(evaluation.get(0));
            writer.println(headers);
            for (EvaluationRun evaluationRun : evaluation) {
                writer.println(evaluationRun.Serialize(separator));
            }
        }
        close();
    }

    private String createHeader(EvaluationRun evaluationRun) {
        Set<EvaluatorType> headers = evaluationRun.getQualititativeMeasures();
        StringBuilder sb = new StringBuilder(stringSize);

        sb.append("ALGORITHM_NAME");
        sb.append(separator);
        sb.append("IMPLEMENTATION_TYPE");
        sb.append(separator);
        sb.append("DATASET");
        sb.append(separator);

        for (EvaluatorType evaluatorType : headers) {
            sb.append(evaluatorType);
            sb.append(separator);
        }
        sb.replace(sb.length()-1, sb.length(), "");
        return sb.toString();

    }

    public String getName() {
        return "EvaluationSerializer";
    }

    public String getFileExtension() {
        return extension;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator= separator;
        if(separator.toLowerCase().equals("\t"))
            extension = "tsv";
        else if(separator.toLowerCase().equals(","))
            extension = "csv";
        else
            extension = "txt";

    }

    public File getFile(String fileName) {
        return new File(folder.getAbsolutePath() + File.separatorChar + fileName);
    }

    public void setFolderPath(File folder) {
        this.folder = folder;
    }
}
