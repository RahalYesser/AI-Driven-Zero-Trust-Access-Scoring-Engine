package com.zerotrust.backend.services.trust;

import com.zerotrust.backend.dto.FeatureVector;

public interface TrustModel {

    double score(FeatureVector features) throws Exception;

}
