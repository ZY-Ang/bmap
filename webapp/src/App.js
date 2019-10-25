import React from 'react';
import './App.css';
import ReactMapGL, {Feature, Layer} from 'react-map-gl';
import WorldMap from 'datamaps/dist/datamaps.all';
import SgpMap from 'datamaps/dist/datamaps.sgp';
import data from './data.json';

// const populationByCountry = d3.json(worldMap);
// const populationsByCountry = d3.tsvParse(data);

// const options = {projectionKey: 'NaturalEarth'};
// use defaults
// optionsList.forEach((options) => {
//     output(`./example/output-${options.projectionKey}`, worldMap(populationsByCountry, options))
// });


class App extends React.Component {
    state = {
        viewport: {},
    };

    componentDidMount() {
        console.log(WorldMap.prototype.worldTopo);
        console.log(SgpMap.prototype.sgpTopo);
        // console.log(DataMap.prototype.canTopo);
    }

    onViewportChange = viewport => {
        const {width, height, ...etc} = viewport;
        this.setState({viewport: etc});
    };

    render() {
        return (
            <div className="App">
                <div className="SearchBar"/>
                <ReactMapGL
                    mapboxApiAccessToken={process.env.REACT_APP_MAPBOX_TOKEN}
                    {...this.state.viewport}
                    width='100%'
                    height='100%'
                    onViewportChange={viewport => this.onViewportChange(viewport)}
                >
                    <Layer type="heatmap" paint={null}>
                        {
                            data.map((element, index) => (
                                <Feature key={index} coordinates={element.latlng} properties={element}/>
                            ))
                        }
                    </Layer>
                </ReactMapGL>
            </div>
        );
    }
}

export default App;
