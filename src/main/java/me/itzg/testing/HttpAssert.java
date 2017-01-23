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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper functions for asserting HTTP behaviors.
 *
 * @author Geoff Bourne
 * @since Jan 2017
 */
public class HttpAssert {

    /**
     * Repeatedly checks the given url until it can connect. When connected, it asserts that the response code
     * is the expected value.
     * @param url the URL to access
     * @param interval the delay in milliseconds between tries
     * @param retries the number of attempts to make. If exhausted, then the {@link IOException} of the final attempt
     *                is thrown
     * @param expectedResponseCode the expected HTTP response code, such as 200 (for OK)
     * @throws InterruptedException if interrupted while sleeping between tries
     * @throws IOException the last exception observed if all tries are exhausted
     */
    public static void waitForEndpoint(URL url, long interval, int retries, int expectedResponseCode) throws InterruptedException, IOException {

        int i = 0;
        while (true) {
            try {
                final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                Assert.assertEquals(expectedResponseCode, httpURLConnection.getResponseCode());

                return;
            } catch (IOException e) {
                if (++i >= retries) {
                    throw e;
                }
                Thread.sleep(interval);
            }
        }
    }
}
