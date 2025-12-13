package com.zerotrust.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.zerotrust.backend.enums.RiskLevel;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.util.Map;

@Service
public class TrustScoringService {

    private Classifier rfModel;

    public TrustScoringService() throws Exception {
        // Initialize Weka Random Forest
        rfModel = new RandomForest();
    }

    /**
     * Train Random Forest on Weka Instances
     */
    public void trainModel(Instances trainingData) throws Exception {
        rfModel.buildClassifier(trainingData);
    }

    /**
     * Compute trust score based on extracted features
     */
    public double computeTrustScore(Map<String, Object> features) throws Exception {
        // Convert Map to Weka Instance
        FastVector attrs = new FastVector();
        attrs.addElement(new Attribute("failedLoginRate"));
        attrs.addElement(new Attribute("nightAccessRate"));
        attrs.addElement(new Attribute("avgDeviceRisk"));
        attrs.addElement(new Attribute("unpatchedDevices"));
        attrs.addElement(new Attribute("vpnAccessRate"));
        attrs.addElement(new Attribute("secondsSinceLastLogin"));
        attrs.addElement(new Attribute("trustScore")); // target

        Instances data = new Instances("UserFeatures", attrs, 0);
        data.setClassIndex(data.numAttributes() - 1);

        double[] vals = new double[data.numAttributes()];
        vals[0] = (double) features.get("failedLoginRate");
        vals[1] = (double) features.get("nightAccessRate");
        vals[2] = (double) features.get("avgDeviceRisk");
        vals[3] = (double) features.get("unpatchedDevices");
        vals[4] = (double) features.get("vpnAccessRate");
        vals[5] = (double) features.get("secondsSinceLastLogin");
        vals[6] = Utils.missingValue(); // target unknown

        Instance instance = new DenseInstance(1.0, vals);
        instance.setDataset(data);

        double score = rfModel.classifyInstance(instance);
        return Math.max(0, Math.min(100, score));
    }

    public RiskLevel getRiskLevel(double score) {
        if(score < 40) return RiskLevel.HIGH;
        if(score < 70) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
}

