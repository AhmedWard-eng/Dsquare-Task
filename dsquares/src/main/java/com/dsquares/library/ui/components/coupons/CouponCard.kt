package com.dsquares.library.ui.components.coupons

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dsquares.library.BuildConfig
import com.dsquares.library.R
import com.dsquares.library.constants.TAG
import com.dsquares.library.di.AppContainer
import com.dsquares.library.ui.models.coupons.CouponUiModel

private const val OVERLAY_BADGE_ALPHA = 0.22f
private const val OVERLAY_LOCK_ALPHA = 0.35f

@Composable
fun CouponCard(
    coupon: CouponUiModel,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = cardShape
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coupon.imageUrl)
                        .crossfade(true)
                        .listener(
                            onError = { request, result ->
                                if (BuildConfig.DEBUG)
                                Log.e(TAG, "Failed to load: ${coupon.imageUrl}", result.throwable)
                            },
                            onSuccess = { request, result ->
                                if (BuildConfig.DEBUG)
                                Log.d(TAG, "Loaded: ${coupon.imageUrl}")
                            }
                        )
                        .build(),
                    contentDescription = coupon.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (!coupon.discount.isNullOrEmpty() && coupon.discount != "0") {
                    DiscountBadge(
                        discount = coupon.discount,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = coupon.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.from_label))
                        append(" ")
                        append(coupon.points.toString())
                    }
                    append(" ")
                    append(stringResource(R.string.points_label))
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (coupon.isLocked) {
            LockOverlay(modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun DiscountBadge(
    discount: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.scrim.copy(alpha = OVERLAY_BADGE_ALPHA),
                shape = RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.discount_percent, discount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LockOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(
            MaterialTheme.colorScheme.scrim.copy(alpha = OVERLAY_LOCK_ALPHA)
        ).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_lock),
            contentDescription = stringResource(R.string.locked),
            tint = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.size(28.dp)
        )

        Text(
            text = stringResource(R.string.upgrade_to_next_package),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            textAlign = TextAlign.Center
        )
    }
}