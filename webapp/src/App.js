import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom";
import logo from './logo.svg';
import './App.css';
import { scaleLinear } from "d3-scale";
import { ComposableMap, Geographies, Geography, Sphere, Graticule } from "react-simple-maps";
//import {getWeightedDataForWordOnce} from './database';

const geoUrl =
    "https://raw.githubusercontent.com/zcreativelabs/react-simple-maps/master/topojson-maps/world-110m.json";

const colorScale = scaleLinear()
    .domain([0.29, 0.68])
    .range(["#ffedea", "#ff5233"]);


class App extends React.Component {
    render() {
        return (
            <div className="App">
                <ComposableMap projectionConfig={{
                    rotate: [-10, 0, 0],
                    scale: 140
                }}>
                    <Sphere stroke="#E4E5E6" strokeWidth={1} />
                    <Graticule stroke="#E4E5E6" strokeWidth={0.5} />
                    <Geographies geography={geoUrl}>
                        {({ geographies }) =>
                            geographies.map(geo => <Geography key={geo.rsmKey} geography={geo} />)
                        }
                        
                    </Geographies>
                </ComposableMap>
            </div>
        );
    }
}

export default App;
