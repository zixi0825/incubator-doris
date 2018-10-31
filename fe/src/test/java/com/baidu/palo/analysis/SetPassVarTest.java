// Modifications copyright (C) 2017, Baidu.com, Inc.
// Copyright 2017 The Apache Software Foundation

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.baidu.palo.analysis;

import com.baidu.palo.common.AnalysisException;
import com.baidu.palo.common.UserException;
import com.baidu.palo.mysql.privilege.MockedAuth;
import com.baidu.palo.mysql.privilege.PaloAuth;
import com.baidu.palo.qe.ConnectContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Mocked;
import mockit.internal.startup.Startup;

public class SetPassVarTest {
    private Analyzer analyzer;

    @Mocked
    private PaloAuth auth;
    @Mocked
    private ConnectContext ctx;

    static {
        Startup.initializeIfPossible();
    }

    @Before
    public void setUp() {
        analyzer = AccessTestUtil.fetchAdminAnalyzer(true);
        MockedAuth.mockedAuth(auth);
        MockedAuth.mockedConnectContext(ctx, "root", "192.168.1.1");
    }

    @Test
    public void testNormal() throws UserException, AnalysisException {
        SetPassVar stmt;

        //  mode: SET PASSWORD FOR 'testUser' = 'testPass';
        stmt = new SetPassVar(new UserIdentity("testUser", "%"), "*88EEBA7D913688E7278E2AD071FDB5E76D76D34B");
        stmt.analyze(analyzer);
        Assert.assertEquals("testCluster:testUser", stmt.getUserIdent().getQualifiedUser());
        Assert.assertEquals("*88EEBA7D913688E7278E2AD071FDB5E76D76D34B", new String(stmt.getPassword()));
        Assert.assertEquals("SET PASSWORD FOR 'testCluster:testUser'@'%' = '*XXX'",
                stmt.toString());

        // empty password
        stmt = new SetPassVar(new UserIdentity("testUser", "%"), null);
        stmt.analyze(analyzer);
        Assert.assertEquals("SET PASSWORD FOR 'testCluster:testUser'@'%' = '*XXX'", stmt.toString());

        // empty user
        // empty password
        stmt = new SetPassVar(null, null);
        stmt.analyze(analyzer);
        Assert.assertEquals("SET PASSWORD FOR 'testCluster:testUser'@'192.168.1.1' = '*XXX'", stmt.toString());
    }

    @Test(expected = AnalysisException.class)
    public void testBadPassword() throws UserException, AnalysisException {
        SetPassVar stmt;
        //  mode: SET PASSWORD FOR 'testUser' = 'testPass';
        stmt = new SetPassVar(new UserIdentity("testUser", "%"), "*88EEBAHD913688E7278E2AD071FDB5E76D76D34B");
        stmt.analyze(analyzer);
        Assert.fail("No exception throws.");
    }

}