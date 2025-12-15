package com.zerotrust.backend.services.trust;

import com.zerotrust.backend.dto.FeatureVector;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.RandomForest;
import weka.core.*;

import java.util.ArrayList;

@Service
public class WekaTrustModel implements TrustModel {

    private final RandomForest model;
    private Instances structure;

    public WekaTrustModel() throws Exception {
        this.model = new RandomForest();
        this.model.setNumIterations(100);
        this.structure = buildStructure();
    }

    private Instances buildStructure() {
        ArrayList<Attribute> attrs = new ArrayList<>();

        attrs.add(new Attribute("failedLoginRate"));
        attrs.add(new Attribute("nightAccessRate"));
        attrs.add(new Attribute("loginFrequency24h"));
        attrs.add(new Attribute("avgDeviceRisk"));
        attrs.add(new Attribute("unpatchedDeviceRatio"));
        attrs.add(new Attribute("antivirusDisabledRatio"));
        attrs.add(new Attribute("networkRiskScore"));
        attrs.add(new Attribute("locationChangeScore"));
        attrs.add(new Attribute("timeAnomalyScore"));
        attrs.add(new Attribute("secondsSinceLastLogin"));

        // Target
        attrs.add(new Attribute("trustScore"));

        Instances data = new Instances("TrustData", attrs, 0);
        data.setClassIndex(attrs.size() - 1);
        return data;
    }

    public void train(Instances trainingData) throws Exception {
        model.buildClassifier(trainingData);
    }

    public RandomForest getClassifier() {
        return model;
    }

    @Override
    public double score(FeatureVector f) throws Exception {
        Instance inst = new DenseInstance(structure.numAttributes());
        inst.setDataset(structure);

        inst.setValue(0, f.getFailedLoginRate());
        inst.setValue(1, f.getNightAccessRate());
        inst.setValue(2, f.getLoginFrequency24h());
        inst.setValue(3, f.getAvgDeviceRisk());
        inst.setValue(4, f.getUnpatchedDeviceRatio());
        inst.setValue(5, f.getAntivirusDisabledRatio());
        inst.setValue(6, f.getNetworkRiskScore());
        inst.setValue(7, f.getLocationChangeScore());
        inst.setValue(8, f.getTimeAnomalyScore());
        inst.setValue(9, f.getSecondsSinceLastLogin());

        inst.setMissing(10);

        double score = model.classifyInstance(inst);
        return Math.max(0, Math.min(100, score));
    }
}
