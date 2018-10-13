/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.network.bio.netty_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
* +--------------+
* | ABC\nDEF\r\n |
* +--------------+
* </pre>
* a {@link DelimiterBasedFrameDecoder}({@link Delimiters#lineDelimiter() Delimiters.lineDelimiter()})
* will choose {@code '\n'} as the first delimiter and produce two frames:
* <pre>
* 
* +-----+----------+-------------+
* | len | filename | file content|
* +-----+----------+-------------+
* 
* 
**/
/**
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 */
public final class NettyClient {

	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

	public static void main(String[] args) throws Exception {

		// 1.1 创建Reactor线程池
		EventLoopGroup group = new NioEventLoopGroup();
		try {// 1.2 创建启动类Bootstrap实例，用来设置客户端相关参数
			Bootstrap b = new Bootstrap();
            b.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);

			b.group(group)// 1.2.1设置线程池
					.channel(NioSocketChannel.class)// 1.2.2指定用于创建客户端NIO通道的Class对象
					.option(ChannelOption.TCP_NODELAY, true)// 1.2.3设置客户端套接字参数
					.handler(new ChannelInitializer<SocketChannel>() {// 1.2.4设置用户自定义handler
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();

							p.addLast(new StringDecoder());
							p.addLast(new NettyClientHandler());

						}
					});

			// 1.3启动链接
			ChannelFuture f = b.connect(HOST, PORT).sync();

			// 1.4 同步等待链接断开
			f.channel().closeFuture().sync();
		} finally {
			// 1.5优雅关闭线程池
			group.shutdownGracefully();
		}
	}
}
