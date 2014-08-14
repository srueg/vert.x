/*
 * Copyright 2014 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version 2.0
 *   (the "License"); you may not use this file except in compliance with the
 *   License.  You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 */

package io.vertx.core.eventbus.impl.codecs;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ByteArrayMessageCodec implements MessageCodec<byte[], byte[]> {

  @Override
  public void encodeToWire(Buffer buffer, byte[] byteArray) {
    buffer.appendInt(byteArray.length);
    buffer.appendBytes(byteArray);
  }

  @Override
  public byte[] decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    pos += 4;
    return buffer.getBytes(pos, pos + length);
  }

  @Override
  public byte[] transform(byte[] bytes) {
    byte[] copied = new byte[bytes.length];
    System.arraycopy(bytes, 0, copied, 0, bytes.length);
    return copied;
  }

  @Override
  public int bodyLengthGuess(byte[] bytes) {
    return 4 + bytes.length;
  }

  @Override
  public String name() {
    return "bytearray";
  }

  @Override
  public byte systemCodecID() {
    return 10;
  }
}
