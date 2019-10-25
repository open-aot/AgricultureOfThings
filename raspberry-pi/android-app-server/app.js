var express = require("express");
var app = express();
const readLastLines = require('read-last-lines');
const date = require('date-and-time');

app.get("/last-day", (req, res, next) => {
	res.setHeader('Access-Control-Allow-Origin', '*');
	//console.log("request for last-day received");
	readLastLines.read('../data/results.txt', 360)
		.then((lines) => {
			var x = '[\n' + lines.substring(0, lines.length - 1) + '\n]';
			obj = JSON.parse(x);
			var counter = 0;
			var objRes = [];
			for (i = 0; i < 360; i = i + 15) {
				var curr = {
					Timestamp: obj[i + 7].Timestamp,
					Moisture: 0,
					Humidity: 0,
					Temperature: 0
				};
				for (j = i; j < i + 15; j++) {
					curr.Moisture = curr.Moisture + obj[j].Moisture;
					curr.Humidity = curr.Humidity + obj[j].Humidity;
					curr.Temperature = curr.Temperature + obj[j].Temperature;
				}
				curr.Moisture = curr.Moisture / 15;
				curr.Humidity = curr.Humidity / 15;
				curr.Temperature = curr.Temperature / 15;
				objRes[counter++] = curr;
			}
			res.status(200).json(objRes);
		}).catch(x => {
			res.status(500).json({});
		});
});

app.get("/last-week", (req, res, next) => {
	res.setHeader('Access-Control-Allow-Origin', '*');
	//console.log("request for last-week received");
	total_read_lines = 2880;
	readLastLines.read('../data/results.txt', total_read_lines)
		.then((lines) => {
			var x = '[\n' + lines.substring(0, lines.length - 1) + '\n]';
			obj = JSON.parse(x);
			obj = obj.map(x => {
				x.Timestamp = date.parse(x.Timestamp.substring(4, x.Timestamp.length).replace("EEST ", ""), 'DD MMM HH:mm:ss YYYY');
				return x;
			})
			var counter = 1;
			var s = {
				Timestamp: obj[0].Timestamp,
				Moisture: obj[0].Moisture,
				Humidity: obj[0].Humidity,
				Temperature: obj[0].Temperature
			};
			var objRes = [];

			for (i = 1; i < obj.length - 1; i++) {
				if (date.isSameDay(obj[i].Timestamp, s.Timestamp)) {
					s.Moisture = s.Moisture + obj[i].Moisture;
					s.Humidity = s.Humidity + obj[i].Humidity;
					s.Temperature = s.Temperature + obj[i].Temperature;
					counter++;
				} else {
					s.Moisture = s.Moisture / counter;
					s.Humidity = s.Humidity / counter;
					s.Temperature = s.Temperature / counter;
					objRes.push(s);
					s = {
						Timestamp: obj[i].Timestamp,
						Moisture: obj[i].Moisture,
						Humidity: obj[i].Humidity,
						Temperature: obj[i].Temperature
					}
					counter = 1;
				}
			}
			s.Moisture = s.Moisture / counter;
			s.Humidity = s.Humidity / counter;
			s.Temperature = s.Temperature / counter;
			objRes.push(s);
			objRes.pop();
			while (objRes.length > 7) {
				objRes.shift();
			}
			objRes = objRes.map(x => {
				//Sat 28 Sep 13:29:42 EEST 2019
				x.Timestamp = date.format(x.Timestamp, 'ddd DD MMM HH:mm:ss Z YYYY').replace("+0300", "EEST");
				return x;
			})
			res.status(200).json(objRes)

		}).catch(x => {
			res.status(500).json({});
		});

})
app.listen(8000, () => {
	console.log("Server running on port 8000");

});