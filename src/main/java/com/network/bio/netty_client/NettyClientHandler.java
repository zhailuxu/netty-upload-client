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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler implementation for the echo client. It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

	private final byte[] request;

	private AtomicInteger atomicInteger = new AtomicInteger(0);

	/**
	 * 创建一个客户端 handler.
	 */
	public NettyClientHandler() {
		request = "hello server,im a client".getBytes();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("--- client already disconnected----");

		ctx.fireChannelInactive();
	}

	public void sendFile(ChannelHandlerContext ctx ,String fileName,boolean closeConnect) {
		FileInputStream fileInputStream = null;
		int count = 0;

		try {
			//3.1 从磁盘加载文件
			File file = new File("/Users/zhuizhumengxiang/Downloads/ServerBootstrap.jpg");
			fileInputStream = new FileInputStream(file);

			byte[] buf = new byte[1024];
			ByteBuf message = null;

			// 文件长度
			System.out.println("send file size:" + fileInputStream.available() + "," + file.length());
			message = Unpooled.buffer(4);
			message.writeInt((int) file.length());
			ctx.write(message);

			// 文件名
			if(fileName.length()<128) {
				int appendNum = 128 - fileName.length();
				for(int i =0;i<appendNum;++i) {
					fileName += " ";
				}
			}
			System.out.println("send file size:" + fileName.length() + "," + file.length());
			message = Unpooled.buffer(128);
			message.writeBytes(fileName.getBytes("utf-8"));
			ctx.write(message);

			int len = -1;
			while (-1 != (len = fileInputStream.read(buf))) {
				System.out.println("--- client send file ----" + len);

				count += len;
				message = Unpooled.wrappedBuffer(buf, 0, len);
				ctx.writeAndFlush(message);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != fileInputStream) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("--- client send file over----" + count);
		if(closeConnect) {
			ctx.channel().close();
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {

		System.out.println("--- client already connected----");
		
		sendFile(ctx, "hello1.jpg",false);
		sendFile(ctx, "hello22.jpg",true);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		System.out.println(atomicInteger.getAndIncrement() + "receive from server:" + msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
