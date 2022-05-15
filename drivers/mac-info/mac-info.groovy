/**
 *  Mac Info Driver
 *
 *  Copyright 2022 Damian Manifold
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Changelog:
 *  2022-05-15 - 0.11  - Initial Develop Commit
 *  2022-04-21 - 0.1   - Initial Coding
 */

metadata {
    definition(name: 'Mac Infot', namespace: 'damianm', author: 'Damian Manifold', importUrl: 'https://raw.githubusercontent.com/dmanifold/hubitat/develop/drivers/mac-info/mac-info.groovy') {
        capability "Actuator"
        capability "PowerSource"
        capability 'Battery'
        attribute 'battery', 'number'
        attribute 'lastUpdated', 'date'
        attribute 'status', 'string'
        attribute 'remaining', 'string'
        attribute 'powerSource', 'powerSource'
        command 'setInfo', [[name: 'Set Info', type: 'STRING', description: 'Enter Output Of BASH Script']]
    }
}
import java.util.regex.Pattern

void setInfo(String valueStr) {
    state.bashOutput = valueStr
    def pattern = Pattern.compile(".*?'(.*?)'\\n\\W*(.*?)\\W*\\(id=(.*?)\\)\\W*(.*?)%;\\W*(.*)\\W*present\\W*(.*)", Pattern.MULTILINE)
    def matcher = pattern.matcher(valueStr)
    matcher.find()
    log.info(matcher[0][0])
    state.powerSource = matcher[0][1]
    state.battery = matcher[0][4] as Integer
    state.status = matcher[0][5].trim()
    switch (state.powerSource) {
        case "AC Power":
            sendEvent(name: 'powerSource', value: 'mains')
            break
        case "Battery Power":
            sendEvent(name: 'powerSource', value: 'battery')
            break
        default:
            sendEvent(name: 'powerSource', value: 'unknown')
    }
    if (state.status.contains("AC attached; not charging")) {
        if (state.battery == 100) {
            sendEvent(name: 'status', value: 'charged')
        } else {
            sendEvent(name: 'status', value: 'not charging')
        }
    } else if (state.status.contains("charged; 0:00 remaining")) {
        sendEvent(name: 'status', value: 'charged')
    } else if (state.status.contains("discharging")) {
        sendEvent(name: 'status', value: 'discharging')
    } else if ((state.status.contains("charging"))) {
        sendEvent(name: 'status', value: 'charging')
    } else {
        sendEvent(name: 'status', value: 'unknown')
    }
    if (state.status.contains("remaining")) {
        def rem = state.status.split(' ')[1]
        def remParts = rem.split(':')
        Integer hours = remParts[0] as Integer
        Integer mins = remParts[1] as Integer
        sendEvent(name: 'remaining', value: Integer.toString((hours * 60) + mins) + " minutes")
    } else {
        sendEvent(name: 'remaining', value: '(no estimate)')
    }
    sendEvent(name: 'battery', value: state.battery)
    Date lastUpdate = new Date()
    sendEvent(name: 'lastUpdated', value: lastUpdate.format('yyyy-MM-dd HH:mm'))
}
