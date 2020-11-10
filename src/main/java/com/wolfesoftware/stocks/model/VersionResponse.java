/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.model;

import java.time.LocalDate;

public class VersionResponse {

    private final String    version;
    private final LocalDate buildDate;
    private final String    gitHash;
    private final String    gitHashFull;

    public VersionResponse(String version, LocalDate buildDate, String gitHash, String gitHashFull) {
        this.version = version;
        this.buildDate = buildDate;
        this.gitHash = gitHash;
        this.gitHashFull = gitHashFull;
    }

    public String getVersion() {
        return version;
    }
    public LocalDate getBuildDate() {
        return buildDate;
    }
    public String getGitHash() {
        return gitHash;
    }
    public String getGitHashFull() {
        return gitHashFull;
    }
}
