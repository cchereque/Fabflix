import React from "react";
import styled from "styled-components";
import {useNavigate} from "react-router-dom";

const StyledDiv = styled.div` 
`

const StyledH1 = styled.h1`
`

const Home = () => {

    const navigate = useNavigate();

    return (
        <div>
            <h1>Home</h1>
            <button onClick={() => navigate("/movie/search")}>Go To Movie Search</button>
        </div>
    );
}

export default Home;
