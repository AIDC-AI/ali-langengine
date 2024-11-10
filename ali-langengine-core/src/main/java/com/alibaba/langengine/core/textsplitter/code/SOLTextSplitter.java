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
package com.alibaba.langengine.core.textsplitter.code;

import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import lombok.Data;

import java.util.Arrays;

/**
 * Attempts to split the text along SOL-formatted headings.
 *
 * @author xiaoxuan.lp
 */
@Data
public class SOLTextSplitter extends RecursiveCharacterTextSplitter {

    public SOLTextSplitter() {
        setSeparators(Arrays.asList(new String[] {
                "\npragma ",
                "\nusing ",
                "\ncontract ",
                "\ninterface ",
                "\nlibrary ",
                "\nconstructor ",
                "\ntype ",
                "\nfunction ",
                "\nevent ",
                "\nmodifier ",
                "\nerror ",
                "\nstruct ",
                "\nenum ",
                "\nif ",
                "\nfor ",
                "\nwhile ",
                "\ndo while ",
                "\nassembly ",
                "\n\n",
                "\n",
                " ",
                "",
        }));
    }
}