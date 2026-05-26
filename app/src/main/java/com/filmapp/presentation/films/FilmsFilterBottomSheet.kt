package com.filmapp.presentation.films

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.filmapp.domain.model.Genre
import com.filmapp.presentation.components.FilmAppButton
import com.filmapp.presentation.components.FilmAppTextButton
import com.filmapp.presentation.components.pressableScale
import com.filmapp.presentation.theme.ChipShape
import com.filmapp.presentation.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilmsFilterBottomSheet(
    genres: List<Genre>,
    initialFilters: FilmsAdvancedFilters,
    onDismiss: () -> Unit,
    onApply: (FilmsAdvancedFilters) -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var draft by remember(initialFilters) { mutableStateOf(initialFilters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Spacing.xxs
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .animateContentSize()
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(bottom = Spacing.xl)
        ) {
            Text(
                text = "Фильтры",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Настройте отображение списка фильмов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            FilterSectionTitle("Год выпуска")
            val yearRange = FilmsAdvancedFilters.DEFAULT_YEAR_MIN.toFloat()..
                FilmsAdvancedFilters.DEFAULT_YEAR_MAX.toFloat()
            Text(
                text = "${draft.yearMin} — ${draft.yearMax}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            RangeSlider(
                value = draft.yearMin.toFloat()..draft.yearMax.toFloat(),
                onValueChange = { range ->
                    draft = draft.copy(
                        yearMin = range.start.toInt(),
                        yearMax = range.endInclusive.toInt()
                    )
                },
                valueRange = yearRange,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            FilterSectionTitle("Жанры")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                genres.forEach { genre ->
                    val selected = genre.id in draft.selectedGenreIds
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.04f else 1f,
                        animationSpec = spring(stiffness = 400f),
                        label = "genreScale"
                    )
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val updated = draft.selectedGenreIds.toMutableSet()
                            if (selected) updated.remove(genre.id) else updated.add(genre.id)
                            draft = draft.copy(selectedGenreIds = updated)
                        },
                        label = { Text(genre.name) },
                        shape = ChipShape,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            FilterSectionTitle("Страна")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    selected = draft.country == null,
                    onClick = { draft = draft.copy(country = null) },
                    label = { Text("Все") },
                    shape = ChipShape
                )
                FilmsAdvancedFilters.POPULAR_COUNTRIES.forEach { country ->
                    FilterChip(
                        selected = draft.country == country,
                        onClick = {
                            draft = draft.copy(
                                country = if (draft.country == country) null else country
                            )
                        },
                        label = { Text(country) },
                        shape = ChipShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            FilterSectionTitle("Минимальный рейтинг")
            Text(
                text = draft.minRating?.let { String.format("%.1f+", it) } ?: "Любой",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            androidx.compose.material3.Slider(
                value = draft.minRating ?: 0f,
                onValueChange = {
                    draft = draft.copy(minRating = if (it <= 0f) null else it)
                },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.sm),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            FilterSectionTitle("Сортировка")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilmSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = draft.sortBy == option,
                        onClick = { draft = draft.copy(sortBy = option) },
                        label = { Text(option.label) },
                        shape = ChipShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilmAppTextButton(
                    text = "Сбросить",
                    onClick = {
                        draft = FilmsAdvancedFilters()
                        onReset()
                    }
                )
                FilmAppButton(
                    text = "Применить",
                    onClick = {
                        onApply(draft)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .pressableScale()
                )
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}
