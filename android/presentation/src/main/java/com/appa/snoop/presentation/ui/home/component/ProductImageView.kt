package com.appa.snoop.presentation.ui.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appa.snoop.presentation.R
import com.appa.snoop.presentation.common.LottieAnim
import com.appa.snoop.presentation.ui.theme.BlueColor
import com.appa.snoop.presentation.ui.theme.WhiteColor
import com.appa.snoop.presentation.util.extensions.noRippleClickable
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp

@Composable
fun ProductImageView(
    modifier: Modifier = Modifier,
    productState: String,
    onLikeClicked: () -> Unit
) {
    var liked by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.sdp))
    ) {
        AsyncImage(
            modifier = modifier
                .width(140.sdp)
                .aspectRatio(1f)
                .align(Alignment.Center),
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://media.istockphoto.com/id/1358386001/photo/apple-macbook-pro.jpg?s=612x612&w=0&k=20&c=d14HA-i0EHpdvNvccdJQ5pAkQt8bahxjjb6fO6hs4E8=")
                .build(),
            contentDescription = "제품 이미지",
            contentScale = ContentScale.FillWidth
        )
        Box(
            modifier = modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(10.sdp))
                .background(color = BlueColor)
        ) {
            Text(
                modifier = modifier
                    .padding(horizontal = 12.sdp, vertical = 4.sdp),
                text = productState,
                style = TextStyle(
                    fontSize = 8.ssp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WhiteColor,
                )
            )
        }
        Box(
            modifier = modifier
                .padding(bottom = 8.sdp, end = 8.sdp)
                .align(Alignment.BottomEnd)
        ) {
            Box(
                modifier = modifier
                    .size(24.sdp)
                    .shadow(
                        elevation = 4.sdp,
                        shape =  CircleShape
                    )
                    .clip(CircleShape)
                    .background(color = WhiteColor)
                    .noRippleClickable {
                        // TODO("서버에 찜목록 추가")
                        onLikeClicked()
                        liked = !liked
                    }
                    .padding(3.sdp)
            ) {
//                Icon(
//                    painter = painterResource(
//                        id = if (liked) R.drawable.ic_filled_like else R.drawable.ic_like
//                    ),
//                    contentDescription = "좋아요",
//                    modifier = modifier.align(Alignment.Center),
//                    tint = if (liked) RedColor else BlackColor
//                )
                LottieAnim(
                    isChecked = isClicked,
                    res = R.raw.lottie_like,
                    startTime = 0.2f,
                    endTime = 0.7f
                )
            }
        }
    }
}

@Composable
@Preview
fun Preview() {
    ProductImageView(
        productState = "지금 최저가"
    ) {}
}