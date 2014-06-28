/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensoc.indexing;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;

import com.opensoc.index.interfaces.IndexAdapter;
import com.opensoc.parser.interfaces.MessageParser;

@SuppressWarnings("rawtypes")
public abstract class AbstractIndexingBolt extends BaseRichBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6710596708304282838L;

	protected static final Logger LOG = LoggerFactory
			.getLogger(AbstractIndexingBolt.class);

	protected OutputCollector _collector;
	protected IndexAdapter _adapter;

	protected String _IndexIP;
	protected int _IndexPort = 0;
	protected String _ClusterName;
	protected String _IndexName;
	protected String _DocumentName;
	protected int _BulkIndexNumber = 10;

	protected String OutputFieldName;

	public final void prepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) {
		_collector = collector;
		
		if (this._IndexIP == null)
			throw new IllegalStateException("_IndexIP must be specified");
		if (this._IndexPort == 0)
			throw new IllegalStateException(
					"_IndexPort must be specified");
		if (this._ClusterName == null)
			throw new IllegalStateException(
					"_ClusterName must be specified");
		if (this._IndexName == null)
			throw new IllegalStateException(
					"_IndexName must be specified");
		if (this._DocumentName == null)
			throw new IllegalStateException(
					"_DocumentName must be specified");
		if (this.OutputFieldName == null)
			throw new IllegalStateException("OutputFieldName must be specified");
		if (this._adapter == null)
			throw new IllegalStateException("IndexAdapter must be specified");
	}

	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declare(new Fields(this.OutputFieldName));
	}

	abstract void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException;

}