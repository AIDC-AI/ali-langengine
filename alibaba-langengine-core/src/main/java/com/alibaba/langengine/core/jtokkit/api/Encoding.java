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
package com.alibaba.langengine.core.jtokkit.api;

public interface Encoding {

    /**
     * Name of the environment variable key to control when JTokkit should switch to a different tokenizer.
     * For all inputs below the given threshold, JTokkit uses a tokenizer that scales in quadratic time but
     * is faster for small inputs. For larger inputs, a linearly scaling tokenizer is used. Per default, when
     * this environment variable is not set, the threshold is configured accordingly to our benchmarks to be
     * near-optimal for a wide variety of use-cases, but if you have a very specialized input format, you may
     * want to experiment and benchmark with different input size thresholds.
     */
    String VERY_LARGE_TOKENIZER_BYTE_THRESHOLD_KEY = "VERY_LARGE_TOKENIZER_BYTE_THRESHOLD";


    /**
     * Encodes the given text into a list of token ids.
     * <p>
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * {@link UnsupportedOperationException}.
     * <p>
     * If you want to encode special tokens as ordinary text, use {@link #encodeOrdinary(String)}.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.encode("hello world");
     * // returns [15339, 1917]
     *
     * encoding.encode("hello &lt;|endoftext|&gt; world");
     * // raises an UnsupportedOperationException
     * </pre>
     *
     * @param text the text to encode
     * @return the list of token ids
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    IntArrayList encode(String text);

    /**
     * Encodes the given text into a list of token ids.
     * <p>
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * {@link UnsupportedOperationException}.
     * <p>
     * If you want to encode special tokens as ordinary text, use {@link #encodeOrdinary(String, int)}.
     * <p>
     * This method will truncate the list of token ids if the number of tokens exceeds the
     * given maxTokens parameter. Note that it will try to keep characters together, that are encoded into
     * multiple tokens. For example, if the text contains a character which is encoded into 3 tokens,
     * and due to the maxTokens parameter the last token of the character is truncated, the first two
     * tokens of the character will also be truncated. Therefore, the actual number of tokens may be
     * less than the given maxTokens parameter.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.encode("hello world", 100);
     * // returns [15339, 1917]
     *
     * encoding.encode("hello &lt;|endoftext|&gt; world", 100);
     * // raises an UnsupportedOperationException
     * </pre>
     *
     * @param text      the text to encode
     * @param maxTokens the maximum number of tokens to encode
     * @return the {@link EncodingResult} containing a list of token ids and whether the tokens were truncated due to the maxTokens parameter
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    EncodingResult encode(String text, int maxTokens);

    /**
     * Encodes the given text into a list of token ids, ignoring special tokens.
     * <p>
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.encodeOrdinary("hello world");
     * // returns [15339, 1917]
     *
     * encoding.encodeOrdinary("hello &lt;|endoftext|&gt; world");
     * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
     * </pre>
     *
     * @param text the text to encode
     * @return the list of token ids
     */
    IntArrayList encodeOrdinary(String text);

    /**
     * Encodes the given text into a list of token ids, ignoring special tokens.
     * <p>
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     * <p>
     * It will truncate the list of token ids if the number of tokens exceeds the
     * given maxTokens parameter. Note that it will try to keep characters together, that are encoded into
     * multiple tokens. For example, if the text contains a character which is encoded into 3 tokens,
     * and due to the maxTokens parameter the last token of the character is truncated, the first two
     * tokens of the character will also be truncated. Therefore, the actual number of tokens may be
     * less than the given maxTokens parameter.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.encodeOrdinary("hello world", 100);
     * // returns [15339, 1917]
     *
     * encoding.encodeOrdinary("hello &lt;|endoftext|&gt; world", 100);
     * // returns [15339, 83739, 8862, 728, 428, 91, 29, 1917]
     * </pre>
     *
     * @param text      the text to encode
     * @param maxTokens the maximum number of tokens to encode
     * @return the {@link EncodingResult} containing a list of token ids and whether the tokens were truncated due to the maxTokens parameter
     */
    EncodingResult encodeOrdinary(String text, int maxTokens);

    /**
     * Encodes the given text into a list of token ids and returns the amount of tokens.
     * It is more performant than {@link #encode(String)}.
     * <p>
     * Special tokens are artificial tokens used to unlock capabilities from a model,
     * such as fill-in-the-middle. There is no support for parsing special tokens
     * in a text, so if the text contains special tokens, this method will throw an
     * {@link UnsupportedOperationException}.
     * <p>
     * If you want to encode special tokens as ordinary text, use {@link #countTokensOrdinary(String)}.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.countTokens("hello world");
     * // returns 2
     *
     * encoding.countTokens("hello &lt;|endoftext|&gt; world");
     * // raises an UnsupportedOperationException
     * </pre>
     *
     * @param text the text to count tokens for
     * @return the amount of tokens
     * @throws UnsupportedOperationException if the text contains special tokens which are not supported for now
     */
    int countTokens(String text);

    /**
     * Encodes the given text into a list of token ids and returns the amount of tokens.
     * It is more performant than {@link #encodeOrdinary(String)}.
     * <p>
     * This method does not throw an exception if the text contains special tokens, but instead
     * encodes them as if they were ordinary text.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.countTokensOrdinary("hello world");
     * // returns 2
     *
     * encoding.countTokensOrdinary("hello &lt;|endoftext|&gt; world");
     * // returns 8
     * </pre>
     *
     * @param text the text to count tokens for
     * @return the amount of tokens
     */
    int countTokensOrdinary(String text);

    /**
     * Decodes the given list of token ids into a text.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.decode(List.of(15339, 1917));
     * // returns "hello world"
     *
     * encoding.decode(List.of(15339, 1917, Integer.MAX_VALUE));
     * // raises an IllegalArgumentException
     * </pre>
     *
     * @param tokens the list of token ids
     * @return the decoded text
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    String decode(IntArrayList tokens);

    /**
     * Decodes the given list of token ids into a byte array.
     * <pre>
     * Encoding encoding = EncodingRegistry.getEncoding(EncodingType.CL100K_BASE);
     * encoding.decodeBytes(List.of(15339, 1917));
     * // returns [104, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100]
     *
     * encoding.decodeBytes(List.of(15339, 1917, Integer.MAX_VALUE));
     * // raises an IllegalArgumentException
     * </pre>
     *
     * @param tokens the list of token ids
     * @return the decoded byte array
     * @throws IllegalArgumentException if the list contains invalid token ids
     */
    byte[] decodeBytes(IntArrayList tokens);

    /**
     * Returns the name of this encoding. This is the name which is used to identify
     * the encoding and must be unique for registration in the {@link EncodingRegistry}.
     *
     * @return the name of this encoding
     */
    String getName();
}
