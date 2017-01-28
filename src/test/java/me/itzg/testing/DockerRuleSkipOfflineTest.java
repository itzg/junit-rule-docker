/*
 * Copyright (c) 2017 by Geoff Bourne <itzgeoff@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.itzg.testing;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Geoff Bourne
 * @since Jan 2017
 */
public class DockerRuleSkipOfflineTest {

    @ClassRule
    public static DockerRule dockerRule = new DockerRule("httpd:alpine")
            .skipWhenOffline(true)
            .uri("http://localhost:22222" /* bogus port */);

    @Test
    public void testGetsSkipped() throws Exception {
        Assert.fail("This shouldn't get here and fail");
    }

    @Test
    public void testGetsSkippedAlso() throws Exception {
        Assert.fail("This shouldn't get here and fail");
    }
}
