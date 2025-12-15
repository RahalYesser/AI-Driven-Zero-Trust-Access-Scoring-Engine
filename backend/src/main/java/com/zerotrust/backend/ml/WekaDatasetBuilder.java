package com.zerotrust.backend.ml;

import com.zerotrust.backend.dto.FeatureVector;
import weka.core.*;

import java.util.ArrayList;

public class WekaDatasetBuilder {

    public static Instances buildDataset(boolean training) {

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

        attrs.add(new Attribute("trustScore")); // target

        Instances dataset = new Instances("TrustFeatures", attrs, 0);
        dataset.setClassIndex(attrs.size() - 1);
        return dataset;
    }

    public static Instance toInstance(FeatureVector f, Instances dataset) {

        double[] v = new double[dataset.numAttributes()];

        v[0] = f.getFailedLoginRate();
        v[1] = f.getNightAccessRate();
        v[2] = f.getLoginFrequency24h();
        v[3] = f.getAvgDeviceRisk();
        v[4] = f.getUnpatchedDeviceRatio();
        v[5] = f.getAntivirusDisabledRatio();
        v[6] = f.getNetworkRiskScore();
        v[7] = f.getLocationChangeScore();
        v[8] = f.getTimeAnomalyScore();
        v[9] = f.getSecondsSinceLastLogin();
        v[10] = Utils.missingValue(); // unknown label

        DenseInstance inst = new DenseInstance(1.0, v);
        inst.setDataset(dataset);
        return inst;
    }
}
