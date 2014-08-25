/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensoc.tagging;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.opensoc.metrics.MetricReporter;
import com.opensoc.tagger.interfaces.TaggerAdapter;

@SuppressWarnings("rawtypes")
public class TelemetryTaggerBolt extends AbstractTaggerBolt {

	/**
	 * OpenSOC telemetry parsing bolt with JSON output
	 */
	private static final long serialVersionUID = -2647123143398352020L;
	private Properties metricProperties;


	public TelemetryTaggerBolt withMessageTagger(TaggerAdapter tagger) {
		_adapter = tagger;
		return this;
	}

	public TelemetryTaggerBolt withOutputFieldName(String OutputFieldName) {
		this.OutputFieldName = OutputFieldName;
		return this;
	}

	public TelemetryTaggerBolt withMetricProperties(Properties metricProperties) {
		this.metricProperties = metricProperties;
		return this;
	}
	
	public TelemetryTaggerBolt withIdentifier(JSONObject identifier)
	{
		this._identifier = identifier;
		return this;
	}

	@Override
	void doPrepare(Map conf, TopologyContext topologyContext,
			OutputCollector collector) throws IOException {

		LOG.info("Preparing TelemetryParser Bolt...");
		//_reporter = new MetricReporter();
		//_reporter.initialize(metricProperties, TelemetryParserBolt.class);
	}

	public void execute(Tuple tuple) {
		System.out.println("------------- I GET MESSAGE: " + tuple.getValue(0));
		JSONObject original_message = (JSONObject) tuple.getValue(0);
		LOG.debug("Received tuple: " + original_message);

		try {

			JSONObject alerts_tag = new JSONObject();
			JSONArray alerts_list = _adapter.tag(original_message);
			LOG.debug("Tagged message: " + alerts_list);
				
			System.out.println("------------- TAGGED: " + alerts_list);
			
			if(original_message.containsKey("alerts"))
			{
				JSONObject tag = (JSONObject) original_message.get("alerts");
				JSONArray already_triggered = (JSONArray) tag.get("triggered");
				alerts_list.addAll(already_triggered);
			}

			alerts_tag.put("identifier", _identifier);
			alerts_tag.put("triggered", alerts_list);
			original_message.put("alerts", alerts_tag);
				

			_collector.ack(tuple);
		//	_reporter.incCounter("com.opensoc.metrics.TelemetryParserBolt.acks");
			_collector.emit(new Values(original_message));
		//	_reporter.incCounter("com.opensoc.metrics.TelemetryParserBolt.emits");

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to tag message :" + original_message);
			_collector.fail(tuple);
		//	_reporter.incCounter("com.opensoc.metrics.TelemetryParserBolt.fails");
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declearer) {
		declearer.declare(new Fields(this.OutputFieldName));

	}
}