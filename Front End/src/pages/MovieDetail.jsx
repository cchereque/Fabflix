import React from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useNavigate, useParams} from "react-router-dom"


const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
  text-align: center;
`

const StyledDiv2 = styled.div`
    display: flex;
    flex-direction: row;
    text-align: center;
`

const StyledH1 = styled.h1`
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`

const MovieDetail = () => {
    const {id} = useParams();
    const [movie, setMovie] = React.useState([]);
    const {register, getValues} = useForm();

    const addToCart = (id) => {
        const payload = {
            movieId: id,
            quantity: getValues("quantity")
        };
        alert("Movie added to cart");
        Idm.cartInsert(payload, localStorage.getItem("access_token")).then(response => console.log("added to cart"))
    };

    React.useEffect(() => {
        Idm.movieDetails(id, localStorage.getItem("access_token"))
            .then(response => setMovie(response.data["movie"]))
    }, []);

    return (
        <StyledDiv>
            {movie &&
                <React.Fragment>
                    <h1>Movie Info</h1>
                    <br></br>
                    <h3>Title:</h3>
                    <p>{movie.title}</p>
                    <br></br>
                    <h3>Release Year:</h3>
                    <p>{movie.year}</p>
                    <br></br>
                    <h3>Director</h3>
                    <p>{movie.director}</p>
                    <br></br>
                    <h3>Budget</h3>
                    <p>{movie.budget}</p>
                    <br></br>
                    <h3>Revenue</h3>
                    <p>{movie.revenue}</p>
                    <br></br>
                    <h3>Overview:</h3>
                    <p>{movie.overview}</p>
                    <br></br>
                    <StyledDiv>
                        <select {...register("quantity")}>
                            <option value={1}>1</option>
                            <option value={2}>2</option>
                            <option value={3}>3</option>
                            <option value={4}>4</option>
                            <option value={5}>5</option>
                            <option value={6}>6</option>
                            <option value={7}>7</option>
                            <option value={8}>8</option>
                            <option value={9}>9</option>
                            <option value={10}>10</option>

                        </select>
                        <StyledButton onClick={() => addToCart(movie.id)}>Add To Cart</StyledButton>
                    </StyledDiv>

                </React.Fragment>
            }

        </StyledDiv>
    );
}

export default MovieDetail;