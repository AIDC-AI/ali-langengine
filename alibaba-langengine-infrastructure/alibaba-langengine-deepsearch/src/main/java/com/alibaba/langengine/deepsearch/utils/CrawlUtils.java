/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.deepsearch.utils;

import com.alibaba.langengine.deepsearch.DeepSearcher;
import com.alibaba.langengine.deepsearch.loader.crawler.BasicCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.util.concurrent.atomic.AtomicInteger;

public class CrawlUtils {

    public static void crawlForDeepSearcher(DeepSearcher deepSearcher, String url) {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder("/tmp/crawler4j/");
        config.setPolitenessDelay(1000);
        config.setMaxDepthOfCrawling(0);
        config.setMaxPagesToFetch(1000);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
//        config.setHaltOnError(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = null;
        try {
            controller = new CrawlController(config, pageFetcher, robotstxtServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        controller.addSeed(url);
        int numberOfCrawlers = 8;
        AtomicInteger numSeenImages = new AtomicInteger();
        CrawlController.WebCrawlerFactory<BasicCrawler> factory = () -> new BasicCrawler(numSeenImages, deepSearcher);
        controller.start(factory, numberOfCrawlers);
    }
}
