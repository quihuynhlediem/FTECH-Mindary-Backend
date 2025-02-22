package com.mindary.aichat.models;

import lombok.Data;

@Data
public class FollowUpAnalysis {

    private boolean needsFollowUp;
    private FollowUpType followUpType;
    private int followUpHours;

    public boolean isNeedsFollowUp() {
        return needsFollowUp;
    }
}
