/**
 * Copyright (C) 2024 AIDC-AI
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
package com.alibaba.langengine.core.chain.retrievalqa;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

public class PromptConstants {

    public static final String QUESTION_PROMPT_TEMPLATE = "Use the following portion of a long document to see if any of the text is relevant to answer the question. \n" +
            "Return any relevant text verbatim.\n" +
            "{context}\n" +
            "Question: {question}\n" +
            "Relevant text, if any:";

    public static final String COMBINE_PROMPT_TEMPLATE = "Given the following extracted parts of a long document and a question, create a final answer with references (\"SOURCES\"). \n" +
            "If you don't know the answer, just say that you don't know. Don't try to make up an answer.\n" +
            "ALWAYS return a \"SOURCES\" part in your answer.\n" +
            "\n" +
            "QUESTION: Which state/country's law governs the interpretation of the contract?\n" +
            "=========\n" +
            "Content: This Agreement is governed by English law and the parties submit to the exclusive jurisdiction of the English courts in  relation to any dispute (contractual or non-contractual) concerning this Agreement save that either party may apply to any court for an  injunction or other relief to protect its Intellectual Property Rights.\n" +
            "Source: 28-pl\n" +
            "Content: No Waiver. Failure or delay in exercising any right or remedy under this Agreement shall not constitute a waiver of such (or any other)  right or remedy.\\n\\n11.7 Severability. The invalidity, illegality or unenforceability of any term (or part of a term) of this Agreement shall not affect the continuation  in force of the remainder of the term (if any) and this Agreement.\\n\\n11.8 No Agency. Except as expressly stated otherwise, nothing in this Agreement shall create an agency, partnership or joint venture of any  kind between the parties.\\n\\n11.9 No Third-Party Beneficiaries.\n" +
            "Source: 30-pl\n" +
            "Content: (b) if Google believes, in good faith, that the Distributor has violated or caused Google to violate any Anti-Bribery Laws (as  defined in Clause 8.5) or that such a violation is reasonably likely to occur,\n" +
            "Source: 4-pl\n" +
            "=========\n" +
            "FINAL ANSWER: This Agreement is governed by English law.\n" +
            "SOURCES: 28-pl\n" +
            "\n" +
            "QUESTION: What did the president say about Michael Jackson?\n" +
            "=========\n" +
            "Content: Madam Speaker, Madam Vice President, our First Lady and Second Gentleman. Members of Congress and the Cabinet. Justices of the Supreme Court. My fellow Americans.  \\n\\nLast year COVID-19 kept us apart. This year we are finally together again. \\n\\nTonight, we meet as Democrats Republicans and Independents. But most importantly as Americans. \\n\\nWith a duty to one another to the American people to the Constitution. \\n\\nAnd with an unwavering resolve that freedom will always triumph over tyranny. \\n\\nSix days ago, Russia’s Vladimir Putin sought to shake the foundations of the free world thinking he could make it bend to his menacing ways. But he badly miscalculated. \\n\\nHe thought he could roll into Ukraine and the world would roll over. Instead he met a wall of strength he never imagined. \\n\\nHe met the Ukrainian people. \\n\\nFrom President Zelenskyy to every Ukrainian, their fearlessness, their courage, their determination, inspires the world. \\n\\nGroups of citizens blocking tanks with their bodies. Everyone from students to retirees teachers turned soldiers defending their homeland.\n" +
            "Source: 0-pl\n" +
            "Content: And we won’t stop. \\n\\nWe have lost so much to COVID-19. Time with one another. And worst of all, so much loss of life. \\n\\nLet’s use this moment to reset. Let’s stop looking at COVID-19 as a partisan dividing line and see it for what it is: A God-awful disease.  \\n\\nLet’s stop seeing each other as enemies, and start seeing each other for who we really are: Fellow Americans.  \\n\\nWe can’t change how divided we’ve been. But we can change how we move forward—on COVID-19 and other issues we must face together. \\n\\nI recently visited the New York City Police Department days after the funerals of Officer Wilbert Mora and his partner, Officer Jason Rivera. \\n\\nThey were responding to a 9-1-1 call when a man shot and killed them with a stolen gun. \\n\\nOfficer Mora was 27 years old. \\n\\nOfficer Rivera was 22. \\n\\nBoth Dominican Americans who’d grown up on the same streets they later chose to patrol as police officers. \\n\\nI spoke with their families and told them that we are forever in debt for their sacrifice, and we will carry on their mission to restore the trust and safety every community deserves.\n" +
            "Source: 24-pl\n" +
            "Content: And a proud Ukrainian people, who have known 30 years  of independence, have repeatedly shown that they will not tolerate anyone who tries to take their country backwards.  \\n\\nTo all Americans, I will be honest with you, as I’ve always promised. A Russian dictator, invading a foreign country, has costs around the world. \\n\\nAnd I’m taking robust action to make sure the pain of our sanctions  is targeted at Russia’s economy. And I will use every tool at our disposal to protect American businesses and consumers. \\n\\nTonight, I can announce that the United States has worked with 30 other countries to release 60 Million barrels of oil from reserves around the world.  \\n\\nAmerica will lead that effort, releasing 30 Million barrels from our own Strategic Petroleum Reserve. And we stand ready to do more if necessary, unified with our allies.  \\n\\nThese steps will help blunt gas prices here at home. And I know the news about what’s happening can seem alarming. \\n\\nBut I want you to know that we are going to be okay.\n" +
            "Source: 5-pl\n" +
            "Content: More support for patients and families. \\n\\nTo get there, I call on Congress to fund ARPA-H, the Advanced Research Projects Agency for Health. \\n\\nIt’s based on DARPA—the Defense Department project that led to the Internet, GPS, and so much more.  \\n\\nARPA-H will have a singular purpose—to drive breakthroughs in cancer, Alzheimer’s, diabetes, and more. \\n\\nA unity agenda for the nation. \\n\\nWe can do this. \\n\\nMy fellow Americans—tonight , we have gathered in a sacred space—the citadel of our democracy. \\n\\nIn this Capitol, generation after generation, Americans have debated great questions amid great strife, and have done great things. \\n\\nWe have fought for freedom, expanded liberty, defeated totalitarianism and terror. \\n\\nAnd built the strongest, freest, and most prosperous nation the world has ever known. \\n\\nNow is the hour. \\n\\nOur moment of responsibility. \\n\\nOur test of resolve and conscience, of history itself. \\n\\nIt is in this moment that our character is formed. Our purpose is found. Our future is forged. \\n\\nWell I know this nation.\n" +
            "Source: 34-pl\n" +
            "=========\n" +
            "FINAL ANSWER: The president did not mention Michael Jackson.\n" +
            "SOURCES:\n" +
            "\n" +
            "QUESTION: {question}\n" +
            "=========\n" +
            "{summaries}\n" +
            "=========\n" +
            "FINAL ANSWER:";
}