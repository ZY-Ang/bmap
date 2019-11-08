import React from "react";
import './App.css';
import { scaleLinear } from "d3-scale";
import { ComposableMap, Geographies, Geography, Sphere, Graticule } from "react-simple-maps";
import {
    subscribeDataForWord,
    subscribeInvocationDistribution,
    unsubscribeDataForWord,
    unsubscribeInvocationDistribution
} from "./database";

const geoUrl =
    "https://raw.githubusercontent.com/zcreativelabs/react-simple-maps/master/topojson-maps/world-110m.json";

const colorScale = scaleLinear()
    .domain([0.29, 0.68])
    .range(["#ffedea", "#ff5233"]);


class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            word: "hello",
            previousWord: "hello",
            invocationDistribution: {},
            wordDistribution: null
        };
    }

    componentDidMount() {
        this.invocationDistributionListener = snapshot => this.setState({
            invocationDistribution: snapshot.val()
        });
        subscribeInvocationDistribution(this.invocationDistributionListener);
        this.getWord();
    }

    getWord = e => {
        if (e) {
            e.preventDefault();
        }
        const {word} = this.state;
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
            this.setState({previousWord: word});
        }
        this.wordDistributionListener = snapshot => this.setState(state => {
            const {invocationDistribution} = this.state;
            let wordDistribution = snapshot.val();
            if (!!wordDistribution && wordDistribution.constructor === Object && Object.keys(invocationDistribution).length > 0) {
                Object.keys(wordDistribution).map(key => (wordDistribution[key] /= invocationDistribution[key]));
                return {wordDistribution};
            } else return {wordDistribution: null};
        });
        subscribeDataForWord(word, this.wordDistributionListener);
    };

    componentWillUnmount() {
        if (!!this.invocationDistributionListener) {
            unsubscribeInvocationDistribution(this.invocationDistributionListener);
        }
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
        }
    }

    render() {
        // TODO: @stephan access data as json object within this.state.wordDistribution
        console.log(this.state.wordDistribution);
        return (
            <div className="App">
                <form
                    style={{
                        position: 'absolute',
                        width: '100%',
                        height: '30%',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center'
                    }}
                    onSubmit={this.getWord}
                >
                    <input
                        type="text"
                        placeholder="hello"
                        value={this.state.word}
                        onChange={e => this.setState({word: e.target.value})}
                    />
                </form>
                <ComposableMap
                    projectionConfig={{
                        rotate: [-11, 0, 0],
                        scale: 155
                    }}
                >
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
